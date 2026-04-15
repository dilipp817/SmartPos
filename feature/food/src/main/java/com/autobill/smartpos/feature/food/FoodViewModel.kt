package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.model.RolePermissions
import com.autobill.smartpos.domain.usecase.GetCategoriesUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsPaginatedUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ObserveRolePermissionsUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel: Food Feature
 *
 * Single responsibility: paginated food loading, category filtering, and session/role state.
 * Cart operations have been extracted to [CartViewModel] — this keeps constructor params
 * at 5, making the ViewModel straightforward to test in isolation:
 *
 *   val vm = FoodViewModel(
 *       getFoodsPaginatedUseCase    = FakeFoodsPaginatedUseCase(successResult),
 *       getRestaurantIdUseCase      = FakeRestaurantIdUseCase(42L),
 *       getCategoriesUseCase        = FakeCategoriesUseCase(emptyList()),
 *       observeRolePermissionsUseCase = FakeRolePermissionsUseCase(RolePermissions.NONE),
 *       observeSessionUseCase       = FakeSessionUseCase(flowOf(null)),
 *   )
 *   vm.loadFirstPage()
 *   assertEquals(UiState.Loading, vm.paginatedFoodsState.value)
 */
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodsPaginatedUseCase: GetFoodsPaginatedUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val observeRolePermissionsUseCase: ObserveRolePermissionsUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    // ========== FOOD STATE ==========

    private val _paginatedFoodsState = MutableStateFlow<UiState<Pagination<Food>>>(UiState.Idle)
    val paginatedFoodsState: StateFlow<UiState<Pagination<Food>>> = _paginatedFoodsState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPagination: Pagination<Food>? = null

    /**
     * Cached restaurantId — sourced from [GetRestaurantIdUseCase] once on ViewModel creation.
     * All restaurant-scoped API calls use this value. NEVER hardcoded.
     */
    private var restaurantId: Long? = null


    // ========== ROLE PERMISSIONS (from session — reactive) ==========

    /**
     * Live role-based UI permissions — updated whenever the session changes.
     * Starts as [RolePermissions.NONE] (no elevated controls) until session loads.
     * Screens observe this to show/hide Cancel Order, Apply Discount, Manage Menu.
     */
    val rolePermissions: StateFlow<RolePermissions> = observeRolePermissionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), RolePermissions.NONE)

    /**
     * Active session user — used by the UI to display dynamic values such as
     * the operator's username in the header.
     * TODO(restaurant-name): Replace username with a restaurant profile name once
     *  GET /restaurant/{id} is added to the backend API contract.
     */
    val sessionUser = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    // ========== FILTER STATE ==========

    private val _selectedTab = MutableStateFlow(OrderTab.OFFLINE)
    val selectedTab: StateFlow<OrderTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow<String?>(null)
    val sortOption: StateFlow<String?> = _sortOption.asStateFlow()

    // ========== CATEGORIES STATE ==========

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _paginatedFoodsState.value = UiState.Error(
                    "No restaurant assigned to this account. " +
                    "Please log in with a counter or manager account."
                )
                return@launch
            }
            loadFirstPage()
            // Load categories for the filter chips
            when (val result = getCategoriesUseCase(restaurantId!!)) {
                is Result.Success -> _categories.value = result.data
                else -> Unit   // non-fatal — filter chips simply stay hidden
            }
        }
    }


    // ========== FILTER OPERATIONS ==========

    fun switchTab(tab: OrderTab) { _selectedTab.value = tab }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
        loadFirstPageWithFilters()
    }

    fun updateSortOption(sort: String?) {
        _sortOption.value = sort
        loadFirstPageWithFilters()
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _sortOption.value = null
        loadFirstPage()
    }

    // ========== PAGINATION ==========

    fun loadFirstPage() {
        viewModelScope.launch {
            _paginatedFoodsState.value = UiState.Loading
            _isLoadingMore.value = false
            val result = getFoodsPaginatedUseCase(restaurantId = restaurantId, offset = 0, limit = 20)
            handlePaginationResult(result, append = false)
        }
    }

    private fun loadFirstPageWithFilters() {
        viewModelScope.launch {
            _paginatedFoodsState.value = UiState.Loading
            _isLoadingMore.value = false
            val result = getFoodsPaginatedUseCase(
                restaurantId = restaurantId,
                offset = 0,
                limit = 20,
                category = _selectedCategory.value,
                // sort not supported by GET /foods/restaurant/{id} — use search endpoint if needed
            )
            handlePaginationResult(result, append = false)
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value) return
        if (currentPagination?.canLoadMore != true) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextOffset = currentPagination!!.offset + currentPagination!!.limit
                val result = getFoodsPaginatedUseCase(restaurantId = restaurantId, offset = nextOffset, limit = 20)
                handlePaginationResult(result, append = true)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun handlePaginationResult(result: PaginationResult<Food>, append: Boolean) {
        when (result) {
            is PaginationResult.Success -> {
                val newPagination = if (append && currentPagination != null) {
                    currentPagination!!.copy(
                        data = currentPagination!!.data + result.pagination.data,
                        currentPage = result.pagination.currentPage,
                        hasMore = result.pagination.hasMore,
                    )
                } else {
                    result.pagination
                }
                currentPagination = newPagination
                _paginatedFoodsState.value = UiState.Success(newPagination)
            }
            is PaginationResult.Failure -> {
                _paginatedFoodsState.value = UiState.Error(
                    message = result.exception.message ?: "Failed to load foods",
                    exception = result.exception,
                )
            }
            PaginationResult.Loading -> _paginatedFoodsState.value = UiState.Loading
        }
    }

    fun retryLoadPaginatedFoods() = loadFirstPage()

    /**
     * Look up a [Food] from the current page by its string id.
     * Used by [HomeRoute] to resolve the full domain object before handing it to
     * [CartViewModel.addToCart] — keeps [CartViewModel] independent of food-list state.
     */
    fun getFoodById(foodId: String): Food? =
        currentPagination?.data?.find { it.id.toString() == foodId }
}

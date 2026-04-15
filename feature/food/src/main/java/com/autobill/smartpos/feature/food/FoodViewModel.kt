package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.model.RolePermissions
import com.autobill.smartpos.domain.usecase.AddToCartUseCase
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.DecreaseCartQuantityUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetCategoriesUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsPaginatedUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.IncreaseCartQuantityUseCase
import com.autobill.smartpos.domain.usecase.ObserveRolePermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel: Food Feature
 * Manages UI state for the Food screen using production-ready patterns.
 * Supports infinite scroll pagination with automatic load-more detection.
 * Uses Result<T> for single operations and PaginationResult<T> for paginated data.
 * Injected with Hilt for dependency management.
 */
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodsUseCase: GetFoodsUseCase,
    private val getFoodsPaginatedUseCase: GetFoodsPaginatedUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val observeRolePermissionsUseCase: ObserveRolePermissionsUseCase,
    // Cart use cases
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val increaseCartQuantityUseCase: IncreaseCartQuantityUseCase,
    private val decreaseCartQuantityUseCase: DecreaseCartQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    // ========== FOOD STATE ==========

    private val _foodsState = MutableStateFlow<UiState<List<Food>>>(UiState.Idle)
    val foodsState: StateFlow<UiState<List<Food>>> = _foodsState.asStateFlow()

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

    // ========== CART STATE (from repository — reactive) ==========

    /** Live cart items — sourced from CartRepository */
    val cartItems: StateFlow<List<CartItem>> = getCartUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // ========== ROLE PERMISSIONS (from session — reactive) ==========

    /**
     * Live role-based UI permissions — updated whenever the session changes.
     * Starts as [RolePermissions.NONE] (no elevated controls) until session loads.
     * Screens observe this to show/hide Cancel Order, Apply Discount, Manage Menu.
     */
    val rolePermissions: StateFlow<RolePermissions> = observeRolePermissionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), RolePermissions.NONE)

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

    // ========== CART OPERATIONS ==========

    /**
     * Add a food item to cart. Looks up from current paginated list.
     * If food is already in cart, quantity is incremented by 1.
     */
    fun addToCart(foodId: String) {
        val food = currentPagination?.data?.find { it.id.toString() == foodId } ?: return
        viewModelScope.launch { addToCartUseCase(food) }
    }

    /** Increase quantity of a cart item by 1 */
    fun increaseQuantity(foodId: String) {
        viewModelScope.launch {
            increaseCartQuantityUseCase(foodId.toLongOrNull() ?: return@launch)
        }
    }

    /** Decrease quantity by 1 — removes item from cart if quantity reaches 0 */
    fun decreaseQuantity(foodId: String) {
        viewModelScope.launch {
            decreaseCartQuantityUseCase(foodId.toLongOrNull() ?: return@launch)
        }
    }

    /** Remove all items from cart */
    fun clearCart() {
        viewModelScope.launch { clearCartUseCase() }
    }

    /** Get current quantity of a food item in cart (0 = not in cart) */
    fun getCartQuantity(foodId: Long): Int =
        cartItems.value.find { it.foodId == foodId }?.quantity ?: 0

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

    // Legacy non-paginated load (kept for backward compat)
    fun loadFoods() {
        viewModelScope.launch {
            _foodsState.value = UiState.Loading
            val result = getFoodsUseCase()
            _foodsState.update {
                when (result) {
                    is Result.Success -> UiState.Success(result.data)
                    is Result.Failure -> UiState.Error(
                        message = result.exception.message ?: "Failed to load foods",
                        exception = result.exception,
                    )
                    Result.Loading -> UiState.Loading
                }
            }
        }
    }

    fun retryLoadFoods() = loadFoods()
    fun retryLoadPaginatedFoods() = loadFirstPage()
}

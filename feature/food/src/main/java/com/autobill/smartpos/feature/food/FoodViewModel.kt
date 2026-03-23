package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.usecase.GetFoodsPaginatedUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {

    // Mutable internal state for foods list (legacy non-paginated)
    private val _foodsState = MutableStateFlow<UiState<List<Food>>>(UiState.Idle)
    
    // Public immutable state for foods
    val foodsState: StateFlow<UiState<List<Food>>> = _foodsState.asStateFlow()

    // Mutable internal state for paginated foods (infinite scroll)
    private val _paginatedFoodsState = MutableStateFlow<UiState<Pagination<Food>>>(UiState.Idle)
    
    // Public immutable state for paginated foods
    val paginatedFoodsState: StateFlow<UiState<Pagination<Food>>> = _paginatedFoodsState.asStateFlow()

    // Track if we're currently loading more
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Current pagination state (for tracking offset and hasMore)
    private var currentPagination: Pagination<Food>? = null

    // ========== CART STATE MANAGEMENT ==========
    
    // Cart items: Map of foodId to quantity
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    // Selected tab (Offline/Online)
    private val _selectedTab = MutableStateFlow(OrderTab.OFFLINE)
    val selectedTab: StateFlow<OrderTab> = _selectedTab.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected category filter
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Sort option
    private val _sortOption = MutableStateFlow<String?>(null)
    val sortOption: StateFlow<String?> = _sortOption.asStateFlow()

    // Initialize with first page
    init {
        loadFirstPage()
    }

    // ========== CART OPERATIONS ==========

    /**
     * Toggle food selection (add/remove from cart)
     */
    fun toggleFoodSelection(foodId: String) {
        _cartItems.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            if (newCart.containsKey(foodId)) {
                newCart.remove(foodId) // Remove if already in cart
            } else {
                newCart[foodId] = 1 // Add with quantity 1
            }
            newCart
        }
    }

    /**
     * Increase quantity of item in cart
     */
    fun increaseQuantity(foodId: String) {
        _cartItems.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            val currentQty = newCart[foodId] ?: 0
            newCart[foodId] = currentQty + 1
            newCart
        }
    }

    /**
     * Decrease quantity of item in cart
     * Removes item if quantity reaches 0
     */
    fun decreaseQuantity(foodId: String) {
        _cartItems.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            val currentQty = newCart[foodId] ?: return@update currentCart
            
            if (currentQty > 1) {
                newCart[foodId] = currentQty - 1
            } else {
                newCart.remove(foodId) // Remove if quantity becomes 0
            }
            newCart
        }
    }

    /**
     * Clear entire cart
     */
    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    /**
     * Check if a food item is in cart
     */
    fun isInCart(foodId: String): Boolean {
        return _cartItems.value.containsKey(foodId)
    }

    /**
     * Get quantity of a food item in cart
     */
    fun getQuantity(foodId: String): Int {
        return _cartItems.value[foodId] ?: 0
    }

    // ========== TAB & FILTER OPERATIONS ==========

    /**
     * Switch between Offline/Online tabs
     */
    fun switchTab(tab: OrderTab) {
        _selectedTab.value = tab
        // TODO: Reload data based on tab if needed
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // TODO: Trigger search API call
    }

    /**
     * Select category filter
     */
    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
        // TODO: Reload data with category filter
        loadFirstPageWithFilters()
    }

    /**
     * Update sort option
     */
    fun updateSortOption(sort: String?) {
        _sortOption.value = sort
        // TODO: Reload data with sort
        loadFirstPageWithFilters()
    }

    /**
     * Reset all filters to default
     */
    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _sortOption.value = null
        loadFirstPage()
    }

    // ========== PAGINATION OPERATIONS ==========

    /**
     * Load first page with current filters
     */
    private fun loadFirstPageWithFilters() {
        viewModelScope.launch {
            _paginatedFoodsState.value = UiState.Loading
            _isLoadingMore.value = false

            // TODO: Pass category and sort to use case when backend is ready
            val result = getFoodsPaginatedUseCase(
                offset = 0,
                limit = 20,
                // category = _selectedCategory.value, // TODO: Uncomment when backend ready
                // sort = _sortOption.value,           // TODO: Uncomment when backend ready
            )
            
            when (result) {
                is PaginationResult.Success -> {
                    currentPagination = result.pagination
                    _paginatedFoodsState.value = UiState.Success(result.pagination)
                }
                is PaginationResult.Failure -> {
                    _paginatedFoodsState.value = UiState.Error(
                        message = result.exception.message ?: "Failed to load foods",
                        exception = result.exception
                    )
                }
                PaginationResult.Loading -> {
                    _paginatedFoodsState.value = UiState.Loading
                }
            }
        }
    }


    /**
     * Loads the first page of foods with pagination.
     * This is called on init to populate the initial state.
     */
    fun loadFirstPage() {
        viewModelScope.launch {
            _paginatedFoodsState.value = UiState.Loading
            _isLoadingMore.value = false

            val result = getFoodsPaginatedUseCase(offset = 0, limit = 20)
            
            when (result) {
                is PaginationResult.Success -> {
                    currentPagination = result.pagination
                    _paginatedFoodsState.value = UiState.Success(result.pagination)
                }
                is PaginationResult.Failure -> {
                    _paginatedFoodsState.value = UiState.Error(
                        message = result.exception.message ?: "Failed to load foods",
                        exception = result.exception
                    )
                }
                PaginationResult.Loading -> {
                    _paginatedFoodsState.value = UiState.Loading
                }
            }
        }
    }

    /**
     * Loads the next page of foods.
     * Called by UI when scroll reaches near bottom.
     * Appends new items to existing list.
     */
    fun loadNextPage() {
        // Prevent duplicate requests and respect hasMore flag
        if (_isLoadingMore.value) return
        if (currentPagination == null || !currentPagination!!.canLoadMore) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            try {
                val nextOffset = currentPagination!!.offset + currentPagination!!.limit
                val result = getFoodsPaginatedUseCase(offset = nextOffset, limit = 20)

                when (result) {
                    is PaginationResult.Success -> {
                        val currentState = currentPagination
                        if (currentState != null) {
                            // Merge new page with existing data
                            val mergedPagination = currentState.copy(
                                data = currentState.data + result.pagination.data,
                                currentPage = result.pagination.currentPage,
                                hasMore = result.pagination.hasMore,
                            )
                            currentPagination = mergedPagination
                            _paginatedFoodsState.value = UiState.Success(mergedPagination)
                        }
                    }
                    is PaginationResult.Failure -> {
                        // On error, keep existing data but show error
                        _paginatedFoodsState.value = UiState.Error(
                            message = result.exception.message ?: "Failed to load more foods",
                            exception = result.exception
                        )
                    }
                    PaginationResult.Loading -> {
                        // Already showing loading indicator via isLoadingMore
                    }
                }
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Legacy method: Loads foods from the use case without pagination.
     * Kept for backward compatibility.
     */
    fun loadFoods() {
        viewModelScope.launch {
            _foodsState.value = UiState.Loading

            val result = getFoodsUseCase()

            _foodsState.update {
                when (result) {
                    is Result.Success -> UiState.Success(result.data)
                    is Result.Failure -> UiState.Error(
                        message = result.exception.message ?: "Failed to load foods",
                        exception = result.exception
                    )
                    Result.Loading -> UiState.Loading
                }
            }
        }
    }

    /**
     * Retries loading foods
     */
    fun retryLoadFoods() {
        loadFoods()
    }

    /**
     * Retries loading paginated foods (first page)
     */
    fun retryLoadPaginatedFoods() {
        loadFirstPage()
    }
}




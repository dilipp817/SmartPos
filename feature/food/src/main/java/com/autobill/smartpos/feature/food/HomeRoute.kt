package com.autobill.smartpos.feature.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.domain.common.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ODRfast Home Route - Integrates new ODRfast UI with existing ViewModel
 * 
 * This composable:
 * 1. Creates ViewModel using Hilt
 * 2. Observes state from ViewModel
 * 3. Converts ViewModel state to NEW HomeScreenData (ODRfast design)
 * 4. Renders HomeScreen with tabs, invoice, cart sidebar, etc.
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onFoodClick: (String) -> Unit = {},
    onCheckoutClick: () -> Unit = {},
) {
    // Get ViewModel instance
    val viewModel: FoodViewModel = hiltViewModel()

    // Observe paginated state
    val paginatedState by viewModel.paginatedFoodsState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    
    // ✅ Observe cart state
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    // Sort dialog state
    var showSortDialog by remember { mutableStateOf(false) }

    // Helper function to get current date/time
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd, yyyy | hh:mm a", Locale.US)
        return dateFormat.format(Date())
    }

    // Build CartSummaryData directly from CartItem list (no food-list lookup needed)
    fun buildCartSummary(): CartSummaryData {
        val cartItemsUI = cartItems.map { item ->
            CartItemUI(
                id = item.foodId.toString(),
                name = item.foodName,
                price = "₹${String.format(Locale.US, "%.2f", item.foodPrice)}",
                quantity = item.quantity,
                subtotal = "₹${String.format(Locale.US, "%.2f", item.subtotal)}",
                imageUrl = item.imageUrl,
            )
        }
        val subtotal = cartItems.sumOf { it.subtotal }
        val tax = subtotal * 0.18
        val total = subtotal + tax
        return CartSummaryData(
            invoice = InvoiceData(
                invoiceNumber = "INV-${System.currentTimeMillis() % 100000}",
                tableNumber = "T-01",
                dateTime = getCurrentDateTime(),
                onChangeInvoice = {},
            ),
            items = cartItemsUI,
            itemCount = cartItems.sumOf { it.quantity },
            subtotal = "₹${String.format(Locale.US, "%.2f", subtotal)}",
            tax = "₹${String.format(Locale.US, "%.2f", tax)}",
            discount = "₹0.00",
            total = "₹${String.format(Locale.US, "%.2f", total)}",
            onQuantityIncrease = { foodId -> viewModel.increaseQuantity(foodId) },
            onQuantityDecrease = { foodId -> viewModel.decreaseQuantity(foodId) },
            onAcceptPayment = onCheckoutClick,
            onClear = { viewModel.clearCart() },
            onReset = { viewModel.resetFilters() },
            onPrint = {},
        )
    }

    fun buildHeader() = HeaderData(
        appTitle = "SmartPos",
        businessName = "Best Business Pvt Ltd",
        selectedTab = selectedTab,
        onTabChange = { tab -> viewModel.switchTab(tab) },
        onProfileClick = {},
    )

    fun buildSearchFilter() = SearchFilterData(
        searchQuery = searchQuery,
        categories = listOf(
            CategoryUI("all", "All", 0),
            CategoryUI("MAIN COURSE", "Main Course", 5),
            CategoryUI("PIZZA", "Pizza", 4),
            CategoryUI("STARTERS", "Starters", 4),
            CategoryUI("DESSERTS", "Desserts", 4),
            CategoryUI("BEVERAGES", "Beverages", 4),
        ),
        selectedCategoryId = selectedCategory,
        sortOption = sortOption ?: "Sort by",
        onSearchChange = { viewModel.updateSearchQuery(it) },
        onCategorySelect = { viewModel.selectCategory(it) },
        onSortClick = { showSortDialog = true },
    )

    // Convert ViewModel state to NEW HomeScreenData (ODRfast design)
    val homeScreenData = when (paginatedState) {
        is UiState.Loading -> {
            HomeScreenData(
                header = buildHeader(),
                searchFilter = buildSearchFilter(),
                foodGrid = FoodGridData(isLoading = true),
                cartSummary = buildCartSummary(),
            )
        }

        is UiState.Success -> {
            val pagination = (paginatedState as UiState.Success).data
            val foodList = pagination.data

            HomeScreenData(
                header = buildHeader(),
                searchFilter = buildSearchFilter(),
                foodGrid = FoodGridData(
                    items = pagination.data.map { food ->
                        val qty = viewModel.getCartQuantity(food.id)
                        FoodItemUI(
                            id = food.id.toString(),
                            name = food.name,
                            price = "₹${String.format(Locale.US, "%.2f", food.price)}",
                            restaurantId = food.restaurantId.toString(),
                            categoryName = food.categoryName,
                            description = food.description,
                            imageUrl = food.imageUrl,
                            isAvailable = food.isAvailable,
                            isSelected = qty > 0,
                            quantity = qty,
                        )
                    },
                    isLoading = false,
                    isLoadingMore = isLoadingMore,
                    canLoadMore = pagination.hasMore,
                    onLoadMore = { viewModel.loadNextPage() },
                    onFoodClick = onFoodClick,
                    onFoodAdd = { foodId -> viewModel.addToCart(foodId) },
                    onFoodIncrease = { foodId -> viewModel.increaseQuantity(foodId) },
                    onFoodDecrease = { foodId -> viewModel.decreaseQuantity(foodId) },
                ),
                cartSummary = buildCartSummary(),
            )
        }

        is UiState.Error -> {
            val errorMessage = (paginatedState as UiState.Error).message

            HomeScreenData(
                header = buildHeader(),
                searchFilter = buildSearchFilter(),
                foodGrid = FoodGridData(
                    hasError = true,
                    errorMessage = errorMessage,
                    onFoodAdd = {},
                    onFoodIncrease = {},
                    onFoodDecrease = {},
                ),
                cartSummary = buildCartSummary(),
            )
        }

        UiState.Idle -> {
            HomeScreenData(
                header = buildHeader(),
                searchFilter = buildSearchFilter(),
                foodGrid = FoodGridData(
                    onLoadMore = { viewModel.loadFirstPage() },
                    onFoodAdd = {},
                    onFoodIncrease = {},
                    onFoodDecrease = {},
                ),
                cartSummary = buildCartSummary(),
            )
        }
    }

    // Show sort dialog when requested
    if (showSortDialog) {
        SortDialog(
            currentSort = sortOption,
            onDismiss = { showSortDialog = false },
            onSortSelected = { sort ->
                viewModel.updateSortOption(sort)
                showSortDialog = false
            }
        )
    }

    // Render the new HomeScreen with all components
    HomeScreen(
        data = homeScreenData,
        modifier = modifier,
    )
}

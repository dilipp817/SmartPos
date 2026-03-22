package com.autobill.smartpos.feature.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    
    // TODO: Add cart state to ViewModel
    // val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    // val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()


    // Helper function to get current date/time
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd, yyyy | hh:mm a", Locale.US)
        return dateFormat.format(Date())
    }

    // Convert ViewModel state to NEW HomeScreenData (ODRfast design)
    val homeScreenData = when (paginatedState) {
        is UiState.Loading -> {
            HomeScreenData(
                header = HeaderData(
                    appTitle = "ODRfast",
                    businessName = "Best business Pvt Ltd",
                    selectedTab = OrderTab.OFFLINE,
                    onTabChange = { /* TODO: Implement tab switching */ },
                    onProfileClick = { /* TODO: Open profile menu */ },
                ),
                searchFilter = SearchFilterData(
                    searchQuery = "",
                    categories = emptyList(),
                    sortOption = "Sort by",
                    onSearchChange = { /* TODO: Implement search */ },
                    onCategorySelect = { /* TODO: Implement category filter */ },
                    onSortClick = { /* TODO: Show sort dialog */ },
                ),
                foodGrid = FoodGridData(
                    items = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    hasError = false,
                    canLoadMore = false,
                    onLoadMore = { viewModel.loadNextPage() },
                    onFoodClick = onFoodClick,
                    onFoodToggle = { /* TODO: Toggle selection */ },
                ),
                cartSummary = CartSummaryData(
                    invoice = InvoiceData(
                        invoiceNumber = "KKB6266629",
                        tableNumber = "23",
                        dateTime = getCurrentDateTime(),
                        onChangeInvoice = { /* TODO: Open invoice dialog */ },
                    ),
                    items = emptyList(),
                    itemCount = 0,
                    subtotal = "₹0.00",
                    tax = "₹0.00",
                    discount = "₹0.00",
                    total = "₹0.00",
                    onQuantityIncrease = { /* TODO: Increase quantity */ },
                    onQuantityDecrease = { /* TODO: Decrease quantity */ },
                    onAcceptPayment = onCheckoutClick,
                    onClear = { /* TODO: Clear cart */ },
                    onReset = { /* TODO: Reset filters */ },
                    onPrint = { /* TODO: Print receipt */ },
                ),
            )
        }

        is UiState.Success -> {
            val pagination = (paginatedState as UiState.Success).data

            HomeScreenData(
                header = HeaderData(
                    appTitle = "ODRfast",
                    businessName = "Best business Pvt Ltd",
                    selectedTab = OrderTab.OFFLINE,
                    onTabChange = { tab ->
                        // TODO: Implement tab switching
                        println("Tab changed to: $tab")
                    },
                    onProfileClick = {
                        // TODO: Open profile menu
                        println("Profile clicked")
                    },
                ),
                searchFilter = SearchFilterData(
                    searchQuery = "",
                    categories = listOf(
                        CategoryUI("all", "All Category", pagination.total),
                        CategoryUI("main", "Main Course", 12),
                        CategoryUI("bev", "Beverages", 8),
                        CategoryUI("dessert", "Desserts", 6),
                        // TODO: Get real categories from API
                    ),
                    sortOption = "Sort by",
                    onSearchChange = { query ->
                        // TODO: Implement search functionality
                        println("Search: $query")
                    },
                    onCategorySelect = { categoryId ->
                        // TODO: Implement category filtering
                        println("Category selected: $categoryId")
                    },
                    onSortClick = {
                        // TODO: Show sort dialog
                        println("Sort clicked")
                    },
                ),
                foodGrid = FoodGridData(
                    items = pagination.data.map { food ->
                        FoodItemUI(
                            id = food.id.toString(),
                            name = food.name,
                            price = "₹${String.format(Locale.US, "%.2f", food.price)}",
                            restaurantId = food.restaurantId.toString(),
                            categoryName = "MAIN COURSE - VEG", // TODO: Get from food model
                            description = null,
                            imageUrl = null,
                            isAvailable = true,
                            isSelected = false, // TODO: Check if in cart
                            rating = null,
                        )
                    },
                    isLoading = false,
                    isLoadingMore = isLoadingMore,
                    hasError = false,
                    canLoadMore = pagination.hasMore,
                    onLoadMore = { viewModel.loadNextPage() },
                    onFoodClick = onFoodClick,
                    onFoodToggle = { foodId ->
                        // TODO: Toggle food selection (add/remove from cart)
                        println("Toggle food: $foodId")
                    },
                ),
                cartSummary = CartSummaryData(
                    invoice = InvoiceData(
                        invoiceNumber = "KKB6266629",
                        tableNumber = "23",
                        dateTime = getCurrentDateTime(),
                        onChangeInvoice = {
                            // TODO: Open change invoice dialog
                            println("Change invoice clicked")
                        },
                    ),
                    items = emptyList(), // TODO: Get from cart state
                    itemCount = 0,
                    subtotal = "₹0.00",
                    tax = "₹0.00",
                    discount = "₹0.00",
                    total = "₹0.00",
                    onQuantityIncrease = { foodId ->
                        // TODO: Increase quantity in cart
                        println("Increase quantity: $foodId")
                    },
                    onQuantityDecrease = { foodId ->
                        // TODO: Decrease quantity in cart
                        println("Decrease quantity: $foodId")
                    },
                    onAcceptPayment = onCheckoutClick,
                    onClear = {
                        // TODO: Clear cart
                        println("Clear cart")
                    },
                    onReset = {
                        // TODO: Reset filters and search
                        println("Reset filters")
                    },
                    onPrint = {
                        // TODO: Print receipt
                        println("Print receipt")
                    },
                ),
            )
        }

        is UiState.Error -> {
            val errorMessage = (paginatedState as UiState.Error).message

            HomeScreenData(
                header = HeaderData(
                    appTitle = "ODRfast",
                    businessName = "Best business Pvt Ltd",
                    selectedTab = OrderTab.OFFLINE,
                    onTabChange = {},
                    onProfileClick = {},
                ),
                searchFilter = SearchFilterData(
                    searchQuery = "",
                    categories = emptyList(),
                    onSearchChange = {},
                    onCategorySelect = {},
                    onSortClick = {},
                ),
                foodGrid = FoodGridData(
                    items = emptyList(),
                    isLoading = false,
                    isLoadingMore = false,
                    hasError = true,
                    errorMessage = errorMessage,
                    canLoadMore = false,
                    onLoadMore = {},
                    onFoodClick = onFoodClick,
                    onFoodToggle = {},
                ),
                cartSummary = CartSummaryData(
                    invoice = InvoiceData(
                        invoiceNumber = "KKB6266629",
                        tableNumber = "23",
                        dateTime = getCurrentDateTime(),
                    ),
                    items = emptyList(),
                    itemCount = 0,
                    subtotal = "₹0.00",
                    tax = "₹0.00",
                    discount = "₹0.00",
                    total = "₹0.00",
                    onAcceptPayment = onCheckoutClick,
                ),
            )
        }

        UiState.Idle -> {
            HomeScreenData(
                header = HeaderData(
                    appTitle = "ODRfast",
                    businessName = "Best business Pvt Ltd",
                    selectedTab = OrderTab.OFFLINE,
                    onTabChange = {},
                    onProfileClick = {},
                ),
                searchFilter = SearchFilterData(
                    searchQuery = "",
                    categories = emptyList(),
                    onSearchChange = {},
                    onCategorySelect = {},
                    onSortClick = {},
                ),
                foodGrid = FoodGridData(
                    items = emptyList(),
                    isLoading = false,
                    isLoadingMore = false,
                    hasError = false,
                    canLoadMore = false,
                    onLoadMore = { viewModel.loadFirstPage() },
                    onFoodClick = onFoodClick,
                    onFoodToggle = {},
                ),
                cartSummary = CartSummaryData(
                    invoice = InvoiceData(
                        invoiceNumber = "KKB6266629",
                        tableNumber = "23",
                        dateTime = getCurrentDateTime(),
                    ),
                    items = emptyList(),
                    itemCount = 0,
                    subtotal = "₹0.00",
                    tax = "₹0.00",
                    discount = "₹0.00",
                    total = "₹0.00",
                    onAcceptPayment = onCheckoutClick,
                ),
            )
        }
    }

    // Render the new HomeScreen with all components
    HomeScreen(
        data = homeScreenData,
        modifier = modifier,
    )
}


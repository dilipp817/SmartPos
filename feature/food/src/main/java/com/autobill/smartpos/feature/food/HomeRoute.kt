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

    // Helper function to find food by ID
    fun findFoodById(foodId: String, foodList: List<com.autobill.smartpos.domain.model.Food>): com.autobill.smartpos.domain.model.Food? {
        return foodList.find { it.id.toString() == foodId }
    }

    // Helper function to calculate subtotal
    fun calculateSubtotal(cartItemsList: List<CartItemUI>): Double {
        return cartItemsList.sumOf { item ->
            // Extract numeric value from price string "₹123.45"
            item.price.removePrefix("₹").toDoubleOrNull()?.times(item.quantity) ?: 0.0
        }
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
                    searchQuery = searchQuery,
                    categories = emptyList(),
                    sortOption = "Sort by",
                    onSearchChange = { query -> viewModel.updateSearchQuery(query) },
                    onCategorySelect = { categoryId -> viewModel.selectCategory(categoryId) },
                    onSortClick = { showSortDialog = true },
                ),
                foodGrid = FoodGridData(
                    items = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    hasError = false,
                    canLoadMore = false,
                    onLoadMore = { viewModel.loadNextPage() },
                    onFoodClick = onFoodClick,
                    onFoodToggle = { foodId -> viewModel.toggleFoodSelection(foodId) },
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
                    onQuantityIncrease = { foodId -> viewModel.increaseQuantity(foodId) },
                    onQuantityDecrease = { foodId -> viewModel.decreaseQuantity(foodId) },
                    onAcceptPayment = onCheckoutClick,
                    onClear = { viewModel.clearCart() },
                    onReset = { viewModel.resetFilters() },
                    onPrint = { /* TODO: Print receipt */ },
                ),
            )
        }

        is UiState.Success -> {
            val pagination = (paginatedState as UiState.Success).data
            val foodList = pagination.data

            // ✅ Map cart items to CartItemUI with real data
            val cartItemsList = cartItems.map { (foodId, quantity) ->
                val food = findFoodById(foodId, foodList)
                if (food != null) {
                    CartItemUI(
                        id = foodId,
                        name = food.name,
                        price = "₹${String.format(Locale.US, "%.2f", food.price)}",
                        quantity = quantity,
                        subtotal = "₹${String.format(Locale.US, "%.2f", food.price * quantity)}",
                        imageUrl = food.imageUrl,
                    )
                } else {
                    // Fallback if food not found in current list
                    CartItemUI(
                        id = foodId,
                        name = "Item #$foodId",
                        price = "₹0.00",
                        quantity = quantity,
                        subtotal = "₹0.00",
                    )
                }
            }

            // ✅ Calculate totals from cart
            val subtotal = calculateSubtotal(cartItemsList)
            val tax = subtotal * 0.18 // 18% GST
            val discount = 0.0 // TODO: Implement discount logic
            val total = subtotal + tax - discount

            HomeScreenData(
                header = HeaderData(
                    appTitle = "ODRfast",
                    businessName = "Best business Pvt Ltd",
                    selectedTab = selectedTab, // ✅ Use real tab state
                    onTabChange = { tab -> viewModel.switchTab(tab) }, // ✅ Wired
                    onProfileClick = {
                        // TODO: Open profile menu
                        println("Profile clicked")
                    },
                ),
                searchFilter = SearchFilterData(
                    searchQuery = searchQuery, // ✅ Use real search state
                    categories = listOf(
                        CategoryUI("all", "All Category", pagination.total),
                        CategoryUI("main", "Main Course", 12),
                        CategoryUI("bev", "Beverages", 8),
                        CategoryUI("dessert", "Desserts", 6),
                        // TODO: Get real categories from API
                    ),
                    selectedCategoryId = selectedCategory, // ✅ Use real category state
                    sortOption = "Sort by",
                    onSearchChange = { query -> viewModel.updateSearchQuery(query) }, // ✅ Wired
                    onCategorySelect = { categoryId -> viewModel.selectCategory(categoryId) }, // ✅ Wired
                    onSortClick = { showSortDialog = true }, // ✅ Wired to show dialog
                ),
                foodGrid = FoodGridData(
                    items = pagination.data.map { food ->
                        FoodItemUI(
                            id = food.id.toString(),
                            name = food.name,
                            price = "₹${String.format(Locale.US, "%.2f", food.price)}",
                            restaurantId = food.restaurantId.toString(),
                            categoryName = food.category ?: "MAIN COURSE", // ✅ Use real category
                            description = food.description,
                            imageUrl = food.imageUrl,
                            isAvailable = food.isAvailable,
                            isSelected = viewModel.isInCart(food.id.toString()), // ✅ Check cart state
                            rating = null,
                        )
                    },
                    isLoading = false,
                    isLoadingMore = isLoadingMore,
                    hasError = false,
                    canLoadMore = pagination.hasMore,
                    onLoadMore = { viewModel.loadNextPage() },
                    onFoodClick = onFoodClick,
                    onFoodToggle = { foodId -> viewModel.toggleFoodSelection(foodId) }, // ✅ Wired
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
                    items = cartItemsList, // ✅ Real cart items
                    itemCount = cartItems.size,
                    subtotal = "₹${String.format(Locale.US, "%.2f", subtotal)}",
                    tax = "₹${String.format(Locale.US, "%.2f", tax)}",
                    discount = "₹${String.format(Locale.US, "%.2f", discount)}",
                    total = "₹${String.format(Locale.US, "%.2f", total)}",
                    onQuantityIncrease = { foodId -> viewModel.increaseQuantity(foodId) }, // ✅ Wired
                    onQuantityDecrease = { foodId -> viewModel.decreaseQuantity(foodId) }, // ✅ Wired
                    onAcceptPayment = onCheckoutClick,
                    onClear = { viewModel.clearCart() }, // ✅ Wired
                    onReset = { viewModel.resetFilters() }, // ✅ Wired
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
                    onSortClick = { showSortDialog = true },
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
                    onSortClick = { showSortDialog = true },
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


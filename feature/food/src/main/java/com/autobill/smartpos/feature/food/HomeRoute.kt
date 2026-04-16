package com.autobill.smartpos.feature.food

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    onFoodClick: (Long) -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToMenuManagement: () -> Unit = {},
) {
    // Get ViewModel instances — FoodViewModel owns food/pagination, CartViewModel owns cart
    val viewModel: FoodViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()

    // Observe food state
    val paginatedState by viewModel.paginatedFoodsState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val rolePermissions by viewModel.rolePermissions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    // Real restaurant name — sourced from GET /restaurants/{id} cache populated by MainViewModel
    val restaurantName by viewModel.restaurantName.collectAsStateWithLifecycle()

    // Observe cart state — all sourced from CartViewModel
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotals by cartViewModel.cartTotals.collectAsStateWithLifecycle()

    // ── String resources ────────────────────────────────────────────────────
    val strNewSale          = stringResource(R.string.new_sale)
    val strDefaultTable     = stringResource(R.string.cart_default_table_number)
    val strAppTitle         = stringResource(R.string.brand_name)
    val strSortByDefault    = stringResource(R.string.sort_by)
    val strLogoutTitle      = stringResource(R.string.logout_dialog_title)
    val strLogoutMessage    = stringResource(R.string.logout_dialog_message)
    val strLogoutConfirm    = stringResource(R.string.logout_confirm)
    val strCancel           = stringResource(R.string.cancel)

    // Sort dialog state
    var showSortDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog state — prevents accidental logout on POS counters
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Helper function to get current date/time
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd, yyyy | hh:mm a", Locale.US)
        return dateFormat.format(Date())
    }

    // Build CartSummaryData — reads pre-computed totals from CartViewModel (no math here)
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
        return CartSummaryData(
            invoice = InvoiceData(
                // No order exists yet — real number assigned by POST /orders at checkout
                invoiceNumber = strNewSale,
                tableNumber = strDefaultTable,
                dateTime = getCurrentDateTime(),
                onChangeInvoice = {},
            ),
            items = cartItemsUI,
            itemCount = cartTotals.itemCount,
            subtotal = "₹${String.format(Locale.US, "%.2f", cartTotals.subtotal)}",
            tax = "₹${String.format(Locale.US, "%.2f", cartTotals.tax)}",
            discount = "₹0.00",
            total = "₹${String.format(Locale.US, "%.2f", cartTotals.total)}",
            onQuantityIncrease = { foodId -> foodId.toLongOrNull()?.let { cartViewModel.increaseQuantity(it) } },
            onQuantityDecrease = { foodId -> foodId.toLongOrNull()?.let { cartViewModel.decreaseQuantity(it) } },
            onAcceptPayment = onCheckoutClick,
            onClear = { cartViewModel.clearCart() },
            onReset = { viewModel.resetFilters() },
            onPrint = {},
            // Discount is applied at bill-generation time in BillingScreen (POST /generate-bill?discount=X).
            // It does not apply at cart stage — hide the button for all roles here.
            canApplyDiscount = false,
            onApplyDiscountClick = {},
        )
    }

    fun buildHeader() = HeaderData(
        appTitle = strAppTitle,
        businessName = restaurantName,
        selectedTab = selectedTab,
        onTabChange = { tab -> viewModel.switchTab(tab) },
        onProfileClick = { showLogoutDialog = true },
        canManageMenu = rolePermissions.canManageMenu,
        onManageMenuClick = onNavigateToMenuManagement,
    )

    fun buildSearchFilter() = SearchFilterData(
        searchQuery = searchQuery,
        categories = categories.map { CategoryUI(id = it.id.toString(), name = it.name) },
        selectedCategoryId = selectedCategory,
        sortOption = sortOption ?: strSortByDefault,
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

            HomeScreenData(
                header = buildHeader(),
                searchFilter = buildSearchFilter(),
                foodGrid = FoodGridData(
                    items = pagination.data.map { food ->
                        val qty = cartViewModel.getCartQuantity(food.id)
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
                    onFoodClick = { idStr -> onFoodClick(idStr.toLong()) },
                    // Resolve the full Food domain object before passing to CartViewModel
                    // so CartViewModel stays independent of food-list state.
                    onFoodAdd = { foodId ->
                        viewModel.getFoodById(foodId)?.let { cartViewModel.addToCart(it) }
                    },
                    onFoodIncrease = { foodId -> foodId.toLongOrNull()?.let { cartViewModel.increaseQuantity(it) } },
                    onFoodDecrease = { foodId -> foodId.toLongOrNull()?.let { cartViewModel.decreaseQuantity(it) } },
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

    // Logout confirmation dialog — shown when profile icon is tapped
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(strLogoutTitle) },
            text = { Text(strLogoutMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(strLogoutConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(strCancel)
                }
            },
        )
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

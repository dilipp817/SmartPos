package com.autobill.smartpos.feature.food

import androidx.compose.runtime.Immutable
import com.autobill.smartpos.domain.model.OrderType

/**
 * Home Screen Data Models - ODRfast Design
 * Immutable data classes for type-safe component communication
 * Only contains required data to minimize recomposition
 */

/**
 * Header data - with tabs and business profile
 */
@Immutable
data class HeaderData(
    val appTitle: String = "",
    val businessName: String = "",
    val businessAvatar: String? = null,
    val selectedTab: OrderTab = OrderTab.OFFLINE,
    val onTabChange: (OrderTab) -> Unit = {},
    val onProfileClick: () -> Unit = {},
    /** Show "Manage Menu" action — visible for admin / super_admin only. */
    val canManageMenu: Boolean = false,
    val onManageMenuClick: () -> Unit = {},
)

/**
 * Order tab enum
 */
enum class OrderTab {
    OFFLINE,
    ONLINE
}

/**
 * Category for filter - minimal required fields
 */
@Immutable
data class CategoryUI(
    val id: String,
    val name: String,
    val itemCount: Int = 0,
)

/**
 * Search and filter data with sort functionality
 */
@Immutable
data class SearchFilterData(
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<CategoryUI> = emptyList(),
    val sortOption: String = "",
    val onSearchChange: (String) -> Unit = {},
    val onCategorySelect: (String) -> Unit = {},
    val onSortClick: () -> Unit = {},
)

/**
 * Single food item for grid - horizontal card with checkbox
 */
@Immutable
data class FoodItemUI(
    val id: String,
    val name: String,
    val price: String,  // Pre-formatted (₹XXX.XX)
    val restaurantId: String,
    val categoryName: String? = null,  // For sticky headers
    val description: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isSelected: Boolean = false,  // true when quantity > 0
    val quantity: Int = 0,            // Current quantity in cart (0 = not in cart)
    val rating: Float? = null,
)

/**
 * Food grid data - pagination support with category headers
 */
@Immutable
data class FoodGridData(
    val items: List<FoodItemUI> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val canLoadMore: Boolean = false,
    val onLoadMore: () -> Unit = {},
    val onFoodClick: (String) -> Unit = {},
    val onFoodAdd: (String) -> Unit = {},      // Add item to cart (or +1 if already present)
    val onFoodIncrease: (String) -> Unit = {}, // Increase quantity of existing cart item
    val onFoodDecrease: (String) -> Unit = {}, // Decrease quantity (removes if reaches 0)
)

/**
 * Cart item with quantity controls
 */
@Immutable
data class CartItemUI(
    val id: String,
    val name: String,
    val price: String,  // Pre-formatted
    val quantity: Int,
    val subtotal: String,  // Pre-formatted
    val imageUrl: String? = null,
)

/**
 * Invoice details
 */
@Immutable
data class InvoiceData(
    val invoiceNumber: String = "",
    val tableNumber: String = "",
    val dateTime: String = "",
    val heldCartCount: Int = 0,
    val onHoldCart: () -> Unit = {},
    val onChangeInvoice: () -> Unit = {},
)

/**
 * Cart summary with invoice and detailed items
 */
@Immutable
data class CartSummaryData(
    val invoice: InvoiceData = InvoiceData(),
    val items: List<CartItemUI> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: String = "₹0.00",  // Pre-formatted
    val tax: String = "₹0.00",       // Pre-formatted
    val discount: String = "₹0.00",  // Pre-formatted
    val total: String = "₹0.00",     // Pre-formatted
    val onQuantityIncrease: (String) -> Unit = {},
    val onQuantityDecrease: (String) -> Unit = {},
    // ── Order type (selected on this screen before checkout) ──────────────
    val selectedOrderType: OrderType = OrderType.DINE_IN,
    val onOrderTypeChange: (OrderType) -> Unit = {},
    // ── Feature-flag driven button logic ─────────────────────────────────
    // TABLE_MANAGEMENT=false or TAKEAWAY → "Place Order" (submit immediately, no table)
    // TABLE_MANAGEMENT=true  + DINE_IN   → "Checkout"   (proceed to table selection)
    val isTableManagementEnabled: Boolean = true,
    val onPlaceOrder: () -> Unit = {},   // TAKEAWAY or TABLE_MANAGEMENT=false
    val onCheckout: () -> Unit = {},     // DINE_IN + TABLE_MANAGEMENT=true
    val onClear: () -> Unit = {},
    val onReset: () -> Unit = {},
    val onPrint: () -> Unit = {},
    /** Show "Apply Discount" button — visible for manager / admin / super_admin only. */
    val canApplyDiscount: Boolean = false,
    val onApplyDiscountClick: () -> Unit = {},
    val onShowHeldCarts: () -> Unit = {},
)

/**
 * Complete home screen state
 */
@Immutable
data class HomeScreenData(
    val header: HeaderData = HeaderData(),
    val searchFilter: SearchFilterData = SearchFilterData(),
    val foodGrid: FoodGridData = FoodGridData(),
    val cartSummary: CartSummaryData = CartSummaryData(),
)

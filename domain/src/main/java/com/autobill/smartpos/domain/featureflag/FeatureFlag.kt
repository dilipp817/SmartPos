package com.autobill.smartpos.domain.featureflag

/**
 * Exhaustive list of every feature flag in SmartPos.
 *
 * Naming convention: <NOUN_PHRASE> that clearly states WHAT the feature is.
 *   - Read as: "is <NOUN_PHRASE> enabled?"
 *   - e.g. isEnabled(OFFLINE_ORDER_SYNC) → "is offline order sync enabled?"
 *
 * Adding a new flag:
 *  1. Add an entry here with a unique [key], sensible [defaultValue], and [description].
 *  2. The flag is immediately available in the debug panel (auto-discovered via entries).
 *  3. To drive it from the backend, add the same [key] to the server's feature-flag response.
 *
 * Priority chain (highest → lowest):
 *  1. Debug override  — set manually in the 🚩 debug panel (debug builds only)
 *  2. Remote flag     — delivered via GET /feature-flags after login
 *  3. [defaultValue]  — compile-time safe fallback
 */
enum class FeatureFlag(
    /** JSON key used in backend responses and DataStore storage. Never rename — backend contract. */
    val key: String,
    /** Value used when neither a remote flag nor a debug override is present. */
    val defaultValue: Boolean,
    /** Human-readable description shown in the debug panel. */
    val description: String,
) {

    // ── Offline & Sync ────────────────────────────────────────────────────────

    /**
     * Enables the offline write queue: orders placed without network are stored locally
     * in pending_orders and synced to the server when connectivity is restored (SyncWorker).
     * Off by default until the backend conflict-recovery story is complete (contract M-13).
     */
    OFFLINE_ORDER_SYNC(
        key          = "is_offline_order_sync_enabled",
        defaultValue = false,
        description  = "Store orders locally when offline and sync to server when connectivity is restored",
    ),

    // ── Real-time & Kitchen ───────────────────────────────────────────────────

    /**
     * Enables the WebSocket connection for live order, table, and KDS updates.
     * When off: no WebSocket is opened, no polling, no push notifications for orders.
     * Used by token-based restaurants where staff do not track orders in real time.
     */
    REALTIME_UPDATES(
        key          = "is_realtime_updates_enabled",
        defaultValue = true,
        description  = "Enable WebSocket real-time updates for orders, tables, and kitchen display",
    ),

    /**
     * Shows the Kitchen Display System screen in the navigation drawer.
     * Can be disabled independently of [REALTIME_UPDATES] —
     * e.g. a restaurant may want live table updates but no KDS screen.
     */
    KITCHEN_DISPLAY(
        key          = "is_kitchen_display_enabled",
        defaultValue = true,
        description  = "Show the Kitchen Display System screen for kitchen staff",
    ),

    // ── Billing & Payment ─────────────────────────────────────────────────────

    /**
     * Shows "Print Bill" / "Print Receipt" actions throughout the billing flow.
     * Disable for restaurants with no printer — billing and payment continue to work normally.
     */
    BILL_PRINTING(
        key          = "is_bill_printing_enabled",
        defaultValue = true,
        description  = "Show print bill and print receipt actions — disable if no printer is available",
    ),

    /**
     * Allows a discount amount or percentage to be applied to a bill before payment.
     * Also gated at runtime by the user's role permissions.
     */
    BILL_DISCOUNT(
        key          = "is_bill_discount_enabled",
        defaultValue = true,
        description  = "Allow discounts to be applied on bills (also gated by role permissions)",
    ),

    /**
     * Allows a single bill to be settled across multiple payment methods
     * (e.g. part cash, part UPI). Off by default — backend split-payment
     * reconciliation story is not yet complete.
     */
    SPLIT_PAYMENT(
        key          = "is_split_payment_enabled",
        defaultValue = false,
        description  = "Allow splitting a single bill across multiple payment methods",
    ),

    // ── Reports & Analytics ───────────────────────────────────────────────────

    /**
     * Shows the Sales Report and Order History screens in the navigation drawer.
     * Disable for restaurants that do not need analytics (e.g. very early stage).
     */
    SALES_REPORTS(
        key          = "is_sales_reports_enabled",
        defaultValue = true,
        description  = "Enable the Sales Reports and Order History screens for admin / manager roles",
    ),

    // ── Restaurant Operating Model ────────────────────────────────────────────
    // These flags shape the entire UI for different restaurant models.
    // See docs/FEATURE_FLAG_IMPLEMENTATION_PLAN.md for full implementation details.

    /**
     * Scenario 1 — shows the online / offline order type toggle in the cart/order flow.
     * Disable for walk-in-only restaurants where all orders are always dine-in.
     */
    ONLINE_ORDER(
        key          = "is_online_order_enabled",
        defaultValue = true,
        description  = "Show online/offline order type toggle — disable for walk-in-only restaurants",
    ),

    /**
     * Scenario 3 — enables table list, table selection during order creation, and table CRUD.
     * Disable for counter-service restaurants where customers sit wherever they like
     * with no reservation or assignment.
     */
    TABLE_MANAGEMENT(
        key          = "is_table_management_enabled",
        defaultValue = true,
        description  = "Enable table list, table selection, and table CRUD — disable for counter-service restaurants",
    ),

    // Scenario 4 uses REALTIME_UPDATES + KITCHEN_DISPLAY above.
    // Scenario 2 uses BILL_PRINTING above.
}

package com.autobill.smartpos.domain.featureflag

/**
 * Exhaustive list of every feature flag in SmartPos.
 *
 * Adding a new flag:
 *  1. Add an entry here with a unique [key], sensible [defaultValue], and [description].
 *  2. The flag is immediately available in the debug panel (auto-discovered via entries).
 *  3. To drive it from the backend, add the same [key] to the server's feature-flag response.
 *
 * Priority chain (highest → lowest):
 *  1. Debug override  — set manually in the 🚩 debug panel (debug builds only)
 *  2. Remote flag     — delivered in the login response (or future remote-config fetch)
 *  3. [defaultValue]  — compile-time safe fallback
 */
enum class FeatureFlag(
    /** JSON key used in backend responses and DataStore storage. */
    val key: String,
    /** Value used when neither a remote flag nor a debug override is present. */
    val defaultValue: Boolean,
    /** Human-readable description shown in the debug panel. */
    val description: String,
) {
    OFFLINE_ORDER_QUEUE(
        key          = "offline_order_queue",
        defaultValue = false,
        description  = "Queue orders locally when offline and sync when network is restored",
    ),
    REAL_TIME_UPDATES(
        key          = "real_time_updates",
        defaultValue = true,
        description  = "Enable WebSocket-based real-time updates for tables, orders, and KDS",
    ),
    KITCHEN_DISPLAY_SYSTEM(
        key          = "kitchen_display_system",
        defaultValue = true,
        description  = "Show the Kitchen Display System screen for kitchen staff",
    ),
    SALES_REPORTS(
        key          = "sales_reports",
        defaultValue = true,
        description  = "Enable the Sales Reports screen for admin / manager roles",
    ),
    APPLY_DISCOUNT(
        key          = "apply_discount",
        defaultValue = true,
        description  = "Allow discounts to be applied on bills (also gated by role permissions)",
    ),
    MULTI_PAYMENT(
        key          = "multi_payment",
        defaultValue = false,
        description  = "Allow splitting a bill across multiple payment methods",
    ),
}


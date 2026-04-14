package com.autobill.smartpos.domain.model

/**
 * Aggregated sales data computed client-side from a [List<Order>] returned by
 * GET /restaurants/{restaurantId}/orders/range?start_date=…&end_date=…
 *
 * Revenue metrics are based on all DELIVERED orders in the period.
 * [orders] retains the full list so callers can drill into individual items.
 */
data class SalesReport(
    val startDate: String,                      // ISO-8601 e.g. "2026-04-13T00:00:00"
    val endDate: String,                        // ISO-8601 e.g. "2026-04-13T23:59:59"
    val totalRevenue: Double,                   // sum of totalAmount for DELIVERED orders
    val orderCount: Int,                        // all orders in the range (excl. CANCELLED)
    val deliveredCount: Int,                    // only DELIVERED orders
    val averageOrderValue: Double,              // totalRevenue / max(1, deliveredCount)
    val topSellingItems: List<TopSellingItem>,  // up to 10 items sorted by quantity sold
    val orders: List<Order>,                    // full raw list for drill-down / history
)

/**
 * Aggregated food-item analytics for the [SalesReport].
 * Built by grouping all [OrderItem]s across all orders in the date range.
 */
data class TopSellingItem(
    val foodId: Long,
    val foodName: String,
    val quantitySold: Int,
    val revenue: Double,     // sum of subtotal across all orders
)


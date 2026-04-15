package com.autobill.smartpos.domain.model

/**
 * Aggregated metrics shown on the Admin Dashboard.
 * All values are computed from data already available via existing repositories.
 */
data class AdminStats(
    /** Total of DELIVERED orders today (matched by createdAt date). */
    val todayRevenue: Double = 0.0,
    /** Number of orders that are PENDING or IN_PROGRESS right now. */
    val activeOrders: Int = 0,
    /** Number of tables in AVAILABLE status. */
    val availableTables: Int = 0,
    /** Number of items in the offline queue (PENDING + FAILED). */
    val offlineQueueCount: Int = 0,
    /** Number of food items marked as unavailable. */
    val unavailableFoodCount: Int = 0,
)


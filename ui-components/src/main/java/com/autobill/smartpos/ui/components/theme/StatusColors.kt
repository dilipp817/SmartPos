package com.autobill.smartpos.ui.components.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralised status colour mappings — single source of truth for ALL status-based
 * colour decisions in the ui-components module.
 *
 * Previously this logic was duplicated across:
 *  - cards/CardComponents.kt   → getTableStatusColor() + getStatusColors()
 *  - badges/BadgeComponents.kt → getStatusColors() (identical copy)
 *  - list/ListItemComponents.kt → getStatusColor() (subset)
 *
 * Rules:
 *  - Status values MUST be passed as the enum's `.value` property (e.g. `TableStatus.OCCUPIED.value`).
 *  - Comparison is case-insensitive (.lowercase()) to handle any legacy string that slips through.
 *  - New statuses must be added here only — no inline when-strings in composables.
 */
object StatusColors {

    // ── Table status colours ──────────────────────────────────────────────────

    /** Background color for a table-status chip/card. */
    fun tableStatusBackground(status: String): Color = when (status.lowercase()) {
        "available"   -> Color(0xFF4CAF50)
        "occupied"    -> Color(0xFFF44336)
        "reserved"    -> Color(0xFFFFC107)
        "cleaning"    -> Color(0xFF2196F3)
        "maintenance" -> Color(0xFF9E9E9E)
        else          -> Color.Gray
    }

    /** (background, text) pair for a table-status badge. */
    fun tableStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
        "available"   -> Color(0xFF4CAF50) to Color.White
        "occupied"    -> Color(0xFFF44336) to Color.White
        "reserved"    -> Color(0xFFFFC107) to Color.Black
        "cleaning"    -> Color(0xFF2196F3) to Color.White
        "maintenance" -> Color(0xFF9E9E9E) to Color.White
        else          -> Color.Gray to Color.White
    }

    // ── Order / general status colours ───────────────────────────────────────

    /** (background, text) pair for any generic status badge. */
    fun generalStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
        "available"  -> Color(0xFF4CAF50) to Color.White
        "occupied"   -> Color(0xFFF44336) to Color.White
        "reserved"   -> Color(0xFFFFC107) to Color.Black
        "pending"    -> Color(0xFF2196F3) to Color.White
        "confirmed"  -> Color(0xFF4CAF50) to Color.White
        "preparing"  -> Color(0xFFFFC107) to Color.Black
        "ready"      -> Color(0xFF9C27B0) to Color.White
        "completed"  -> Color(0xFF9C27B0) to Color.White
        "cancelled"  -> Color(0xFFB00020) to Color.White
        "unpaid"     -> Color(0xFFFF6F00) to Color.White
        "partial"    -> Color(0xFFFFC107) to Color.Black
        "paid"       -> Color(0xFF4CAF50) to Color.White
        "cleaning"   -> Color(0xFF2196F3) to Color.White
        else         -> Color.Gray to Color.White
    }

    /** (background, text) pair for an order-status badge. */
    fun orderStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
        "pending"   -> Color(0xFF2196F3) to Color.White
        "confirmed" -> Color(0xFF4CAF50) to Color.White
        "preparing" -> Color(0xFFFFC107) to Color.Black
        "ready"     -> Color(0xFF9C27B0) to Color.White
        "completed" -> Color(0xFF4CAF50) to Color.White
        "cancelled" -> Color(0xFFB00020) to Color.White
        else        -> Color.Gray to Color.White
    }
}


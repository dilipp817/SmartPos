package com.autobill.smartpos.ui.components.layout

import androidx.compose.ui.unit.dp

/**
 * Centralised layout dimension tokens for SmartPos.
 *
 * Use these instead of raw dp literals so that breakpoints can be tuned in
 * one place and stay consistent across all screens.
 *
 * Naming convention:
 *   MAX_WIDTH_*   — widthIn(max = …) caps that prevent content over-stretching on wide tablets
 *   GRID_CELL_*   — GridCells.Adaptive(minSize = …) minimum cell sizes
 */
object LayoutTokens {

    // ── Content max-width caps ────────────────────────────────────────────────

    /** Standard list / form screens (orders, reports, admin, settings). */
    val MAX_WIDTH_CONTENT = 840.dp

    /** Focused transaction screens (billing, payment). */
    val MAX_WIDTH_FOCUSED = 640.dp

    /** Auth / onboarding screens (login). */
    val MAX_WIDTH_AUTH = 480.dp

    /** Wide two-column screens (create order). */
    val MAX_WIDTH_WIDE = 1000.dp

    /** Full catalogue grid screens (food screen outer wrapper). */
    val MAX_WIDTH_CATALOGUE = 1200.dp

    /** Narrow header widgets (tab rows, badges). */
    val MAX_WIDTH_HEADER_WIDGET = 400.dp

    // ── Adaptive grid cell min-sizes ──────────────────────────────────────────

    /** Food grid cards — 2 cols on 8", 3+ cols on 14". */
    val GRID_CELL_FOOD = 260.dp

    /** Table grid cards — more columns on wider screens. */
    val GRID_CELL_TABLE = 180.dp

    /** Kitchen display order cards — large cards, fewer columns. */
    val GRID_CELL_KITCHEN = 320.dp
}


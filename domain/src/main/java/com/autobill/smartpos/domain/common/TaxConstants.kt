package com.autobill.smartpos.domain.common

/**
 * Tax-related constants shared across features.
 *
 * ⚠️  These values are LOCAL ESTIMATES for UX preview only.
 *     The real breakdown (CGST 9% + SGST 9%) is always computed server-side
 *     via POST /orders/{id}/generate-bill — never use these for actual payment.
 */
object TaxConstants {
    /** Estimated GST rate: 18% (9% CGST + 9% SGST). */
    const val GST_ESTIMATE_RATE = 0.18

    // ── Receipt / bill-print rates (hardcoded v1 — Indian restaurant) ─────────

    /**
     * CGST rate applied when printing from Order Detail or Home Screen where no
     * [Bill] exists yet. Billing Screen always uses server-computed values.
     *
     * Current value: 2.5% (standard restaurant GST slab for B2C, India).
     * Configurable tax rates are a future enhancement.
     */
    const val RECEIPT_CGST_RATE = 0.025   // 2.5%

    /** SGST rate — mirrors [RECEIPT_CGST_RATE] (intra-state supply). */
    const val RECEIPT_SGST_RATE = 0.025   // 2.5%
}


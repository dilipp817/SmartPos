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
}


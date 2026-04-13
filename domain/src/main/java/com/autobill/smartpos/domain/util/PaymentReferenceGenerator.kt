package com.autobill.smartpos.domain.util

import java.util.UUID

/**
 * Generates idempotent payment reference numbers.
 *
 * ## Why this matters
 * The payment API uses [referenceNumber] as an idempotency key:
 *  - If the network drops mid-request and you retry with the **same** referenceNumber,
 *    and the first attempt already succeeded, the server returns the existing payment.
 *    → No double charge.
 *  - If the returned payment status is FAILED, generate a **new** referenceNumber for
 *    the next attempt — the backend deduplicates on this key per-status.
 *
 * ## Format
 *   REF-{yyyyMMdd}-{UUID-v4-first-8-chars}
 *   Example: REF-20260413-3f2a1b0c
 *
 * ## Usage
 * ```kotlin
 * val refNumber = PaymentReferenceGenerator.generate()
 * // Store it before the network call. If the call fails with a network error,
 * // retry with the SAME refNumber. Generate a new one only if status = FAILED.
 * ```
 *
 * ## Thread safety
 * [UUID.randomUUID] is thread-safe. This object has no mutable state.
 */
object PaymentReferenceGenerator {

    /**
     * Generate a unique reference number for a single payment attempt.
     *
     * Always call this ONCE per attempt and persist the result before making the API call.
     * Do NOT generate a new one on network-error retries — only on FAILED payments.
     */
    fun generate(): String {
        val date = currentDateString()
        val uuid = UUID.randomUUID().toString().replace("-", "").take(12)
        return "REF-$date-$uuid"
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun currentDateString(): String {
        val cal = java.util.Calendar.getInstance()
        val y = cal.get(java.util.Calendar.YEAR)
        val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y$m$d"
    }
}


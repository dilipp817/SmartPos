package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.remote.dto.BillDto
import com.autobill.smartpos.data.remote.dto.BillItemDto
import com.autobill.smartpos.data.remote.dto.BillSummaryDto
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillItem
import com.autobill.smartpos.domain.model.BillStatus

// ── BillItemDto → Domain ─────────────────────────────────────────────────────

fun BillItemDto.toDomain(): BillItem = BillItem(
    id        = id,
    billId    = billId,
    foodId    = foodId,
    foodName  = foodName,
    quantity  = quantity,
    unitPrice = unitPrice,
    itemTotal = itemTotal,
    createdAt = createdAt,
)

// ── BillDto → Domain ──────────────────────────────────────────────────────────

fun BillDto.toDomain(): Bill = Bill(
    id              = id,
    billNumber      = billNumber,
    orderId         = orderId,
    restaurantId    = restaurantId,
    restaurantName  = restaurantName,
    subtotal        = subtotal,
    taxAmount       = taxAmount,
    cgstAmount      = cgstAmount,
    sgstAmount      = sgstAmount,
    discountAmount  = discountAmount,
    totalAmount     = totalAmount,
    paidAmount      = paidAmount,
    remainingAmount = remainingAmount,
    status          = BillStatus.fromValue(status),
    billItems       = billItems.map { it.toDomain() },
    createdAt       = createdAt,
    updatedAt       = updatedAt,
)

// ── BillSummaryDto → Domain ───────────────────────────────────────────────────
// Summary responses omit tax breakdown and line items — those fields are zeroed.
// Use getBillById() to fetch the full Bill when those values are needed.

fun BillSummaryDto.toDomain(): Bill = Bill(
    id              = id,
    billNumber      = billNumber,
    orderId         = orderId,
    restaurantId    = 0L,            // not present in summary
    restaurantName  = restaurantName,
    subtotal        = totalAmount,   // best approximation — full breakdown not in summary
    taxAmount       = 0.0,
    cgstAmount      = 0.0,
    sgstAmount      = 0.0,
    discountAmount  = 0.0,
    totalAmount     = totalAmount,
    paidAmount      = 0.0,
    remainingAmount = totalAmount,
    status          = BillStatus.fromValue(status),
    billItems       = emptyList(),
    createdAt       = createdAt,
    updatedAt       = createdAt,     // not in summary — reuse createdAt
)



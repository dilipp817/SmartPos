package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.remote.dto.PaymentDto
import com.autobill.smartpos.domain.model.Payment
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus

// ── PaymentDto → Domain ───────────────────────────────────────────────────────

fun PaymentDto.toDomain(): Payment = Payment(
    id              = id,
    billId          = billId,
    orderId         = orderId,
    paymentMethod   = PaymentMethod.fromValue(paymentMethod),
    amount          = amount,
    status          = PaymentStatus.fromValue(status),
    referenceNumber = referenceNumber,
    transactionId   = transactionId,
    changeAmount    = changeAmount,
    notes           = notes,
    createdAt       = createdAt,
    updatedAt       = updatedAt,
)


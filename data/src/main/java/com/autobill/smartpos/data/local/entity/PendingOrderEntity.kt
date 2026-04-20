package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Room entity: one row = one offline-queued [createOrder] call.
 *
 * [itemsJson]      — Moshi-serialised [List<PendingOrderItem>]
 * [status]         — PENDING → SYNCING → deleted (success) | FAILED (409 conflict)
 * [failureReason]  — human-readable reason set on FAILED; shown in the queue UI
 * [createdAt]      — epoch-ms; used for FIFO ordering during sync
 */
@Entity(
    tableName = "pending_orders",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class PendingOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restaurantId: Long,
    val tableId: Long?,              // null for TAKEAWAY / TABLE_MANAGEMENT=false orders
    val orderType: String,           // DINE_IN | TAKEAWAY | DELIVERY
    val notes: String?,
    val itemsJson: String,           // JSON: List<PendingOrderItem>
    val status: String = STATUS_PENDING,
    val failureReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val STATUS_PENDING  = "PENDING"
        const val STATUS_SYNCING  = "SYNCING"
        const val STATUS_FAILED   = "FAILED"
    }
}

/**
 * One item in the serialised [PendingOrderEntity.itemsJson] array.
 * Maps 1:1 to [OrderItemRequestDto] — converted just before the API call.
 */
@JsonClass(generateAdapter = true)
data class PendingOrderItem(
    val foodId: Long,
    val quantity: Int,
    val specialRequests: String? = null,
)


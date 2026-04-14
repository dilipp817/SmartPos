package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Thin type-probe envelope — parsed first to read the [type] discriminator,
 * then the full raw string is re-parsed with the correct typed DTO.
 */
@JsonClass(generateAdapter = true)
data class WsTypeProbeDto(
    @param:Json(name = "type") val type: String,
)

/**
 * Full envelope for ORDER_CREATED / ORDER_UPDATED / ORDER_ITEM_UPDATED.
 * [data] carries the full order — same shape as REST [OrderDto].
 */
@JsonClass(generateAdapter = true)
data class WsOrderEventDto(
    @param:Json(name = "type")      val type:      String,
    @param:Json(name = "data")      val data:      OrderDto?,
    @param:Json(name = "timestamp") val timestamp: String? = null,
)

/**
 * Full envelope for TABLE_UPDATED.
 * [data] carries the updated table — same shape as REST [TableDto].
 */
@JsonClass(generateAdapter = true)
data class WsTableEventDto(
    @param:Json(name = "type")      val type:      String,
    @param:Json(name = "data")      val data:      TableDto?,
    @param:Json(name = "timestamp") val timestamp: String? = null,
)

/** Outbound heartbeat sent by the client every 30 s. */
@JsonClass(generateAdapter = true)
data class WsPingMessageDto(
    @param:Json(name = "type") val type: String = "PING",
)

/** Well-known inbound message type strings from the server. */
object WsEventType {
    const val ORDER_CREATED      = "ORDER_CREATED"
    const val ORDER_UPDATED      = "ORDER_UPDATED"
    const val ORDER_ITEM_UPDATED = "ORDER_ITEM_UPDATED"
    const val TABLE_UPDATED      = "TABLE_UPDATED"
    const val CONNECTED          = "CONNECTED"
    const val PONG               = "PONG"
}


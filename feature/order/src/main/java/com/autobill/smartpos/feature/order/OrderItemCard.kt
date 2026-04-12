package com.autobill.smartpos.feature.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.ui.components.badges.OrderStatusBadge

/**
 * Card representing a single [Order] row in the Order List.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────────┐
 * │  ORD-001           [IN_PROGRESS badge]                  │
 * │  Table T-3 · 3 items · DINE_IN                         │
 * │  ₹450.00                         13 Apr, 10:32 AM      │
 * └─────────────────────────────────────────────────────────┘
 */
@Composable
fun OrderItemCard(
    order: Order,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = Color(order.status.containerColor())
    val accentColor    = Color(order.status.accentColor())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick(order.id) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // ── Row 1: Order number + status badge ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                OrderStatusBadge(status = order.status.value)
            }

            // ── Row 2: Table · item count · order type ───────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Table ${order.tableNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E),
                )
                Text(
                    text = "${order.items.size} item${if (order.items.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161),
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E),
                )
                Text(
                    text = order.orderType.value.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ── Row 3: Total amount + relative time ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "₹%.2f".format(order.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = formatOrderTime(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                )
            }

            // ── Notes (if any) ───────────────────────────────────────────
            val notes = order.notes
            if (!notes.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor.copy(alpha = 0.5f)),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Formats an ISO-8601 datetime string (e.g. "2026-04-13T10:32:00")
 * to a readable short form like "13 Apr, 10:32 AM".
 *
 * Falls back to the raw string on parse failure.
 */
private fun formatOrderTime(createdAt: String): String {
    return try {
        // Split on "T" separator: "2026-04-13T10:32:00" → ["2026-04-13", "10:32:00"]
        val parts = createdAt.split("T")
        if (parts.size < 2) return createdAt
        val datePart = parts[0]   // "2026-04-13"
        val timePart = parts[1].take(5)  // "10:32"

        val dateSections = datePart.split("-")
        if (dateSections.size < 3) return createdAt

        val day = dateSections[2].toIntOrNull() ?: return createdAt
        val month = when (dateSections[1]) {
            "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
            "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
            "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
            else -> dateSections[1]
        }

        val timeHour = timePart.split(":")[0].toIntOrNull() ?: return createdAt
        val timeMin  = timePart.split(":").getOrNull(1) ?: "00"
        val amPm     = if (timeHour < 12) "AM" else "PM"
        val hour12   = when {
            timeHour == 0  -> 12
            timeHour > 12  -> timeHour - 12
            else           -> timeHour
        }

        "$day $month, $hour12:$timeMin $amPm"
    } catch (e: Exception) {
        createdAt
    }
}




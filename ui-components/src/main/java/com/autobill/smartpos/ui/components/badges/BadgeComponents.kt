package com.autobill.smartpos.ui.components.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Status Badge - Display status with color coding
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = getStatusColors(status)

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small),
        color = bgColor,
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Count Badge - Display count notifications
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return

    Surface(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.error,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// Tag Badge - Display tags/categories
@Composable
fun TagBadge(
    tag: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    textColor: Color = Color.White,
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small),
        color = backgroundColor,
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

// Order Status Badge - Color-coded order status
@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFF2196F3) to Color.White
        "confirmed" -> Color(0xFF4CAF50) to Color.White
        "preparing" -> Color(0xFFFFC107) to Color.Black
        "ready" -> Color(0xFF9C27B0) to Color.White
        "completed" -> Color(0xFF4CAF50) to Color.White
        "cancelled" -> Color(0xFFB00020) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small),
        color = bgColor,
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Table Status Badge - Color-coded table status
@Composable
fun TableStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "available" -> Color(0xFF4CAF50) to Color.White
        "occupied" -> Color(0xFFF44336) to Color.White
        "reserved" -> Color(0xFFFFC107) to Color.Black
        "cleaning" -> Color(0xFF2196F3) to Color.White
        "maintenance" -> Color(0xFF9E9E9E) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small),
        color = bgColor,
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Numeric Badge - Display numbers with badge styling
@Composable
fun NumericBadge(
    number: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// Helper function for status colors
@Composable
private fun getStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "available" -> Color(0xFF4CAF50) to Color.White
        "occupied" -> Color(0xFFF44336) to Color.White
        "reserved" -> Color(0xFFFFC107) to Color.Black
        "pending" -> Color(0xFF2196F3) to Color.White
        "confirmed" -> Color(0xFF4CAF50) to Color.White
        "completed" -> Color(0xFF9C27B0) to Color.White
        "unpaid" -> Color(0xFFFF6F00) to Color.White
        "partial" -> Color(0xFFFFC107) to Color.Black
        "paid" -> Color(0xFF4CAF50) to Color.White
        else -> Color.Gray to Color.White
    }
}


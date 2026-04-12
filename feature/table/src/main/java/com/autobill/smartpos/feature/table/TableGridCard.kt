package com.autobill.smartpos.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Icon
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
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Card for a single table in the grid.
 *
 * - AVAILABLE  → fully tappable via [onClick], green tint, no edit hint
 * - All others → tappable via [onChangeStatus] to open the status-update dialog;
 *               shows a subtle pencil icon so staff know the card is interactive
 */
@Composable
fun TableGridCard(
    table: Table,
    onClick: () -> Unit,
    onChangeStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelectable   = table.status == TableStatus.AVAILABLE
    val containerColor = Color(table.status.containerColor())
    val contentColor   = Color(table.status.contentColor())
    val borderColor    = if (isSelectable) contentColor.copy(alpha = 0.4f) else Color(0xFFE0E0E0)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = if (isSelectable) onClick else onChangeStatus),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelectable) containerColor else Color(0xFFF5F5F5),
        tonalElevation = if (isSelectable) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Table icon + number + optional edit hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = if (isSelectable) contentColor else Color(0xFFBDBDBD),
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Table ${table.tableNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelectable) contentColor else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f),
                )
                // Edit hint for non-available tables
                if (!isSelectable) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Change status",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Floor + capacity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoChip(label = "Floor ${table.floor}", color = Color(0xFF757575))
                InfoChip(
                    label = "👥 ${table.capacity}",
                    color = if (isSelectable) contentColor else Color(0xFF9E9E9E),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Status chip
            StatusChip(status = table.status)
        }
    }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun StatusChip(status: TableStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(status.containerColor()))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.value,
            style = MaterialTheme.typography.labelSmall,
            color = Color(status.contentColor()),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

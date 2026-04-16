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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Card for a single table in the grid.
 *
 * Tap behaviour:
 *  - AVAILABLE + !canManageTables → [onClick] (select for order)
 *  - AVAILABLE + canManageTables  → [onClick] (select for order); ⋮ opens Edit/Delete
 *  - Other status                 → [onChangeStatus] (open status dialog); ⋮ opens Edit/Delete
 *
 * The `MoreVert` (⋮) overflow icon is only rendered when [canManageTables] = true.
 */
@Composable
fun TableGridCard(
    table: Table,
    onClick: () -> Unit,
    onChangeStatus: () -> Unit,
    canManageTables: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isSelectable   = table.status == TableStatus.AVAILABLE
    val containerColor = Color(table.status.containerColor())
    val contentColor   = Color(table.status.contentColor())
    val borderColor    = if (isSelectable) contentColor.copy(alpha = 0.4f) else Color(0xFFE0E0E0)

    var menuExpanded by remember { mutableStateOf(false) }

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
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Top row: icon + number + overflow menu ────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = if (isSelectable) contentColor else Color(0xFFBDBDBD),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(R.string.table_card_number, table.tableNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelectable) contentColor else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f),
                )
                when {
                    // Admin/manager → overflow menu (⋮)
                    canManageTables -> {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.cd_table_options),
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.table_menu_edit)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                    onClick = { menuExpanded = false; onEdit() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.table_menu_delete), color = Color(0xFFC62828)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                    onClick = { menuExpanded = false; onDelete() },
                                )
                            }
                        }
                    }
                    // Staff on non-available table → hint to tap for status change
                    !isSelectable -> {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = stringResource(R.string.cd_change_status),
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // ── Floor + capacity ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoChip(label = stringResource(R.string.table_card_floor, table.floor), color = Color(0xFF757575))
                InfoChip(
                    label = "👥 ${table.capacity}",
                    color = if (isSelectable) contentColor else Color(0xFF9E9E9E),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ── Status chip ───────────────────────────────────────────────
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

package com.autobill.smartpos.feature.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Sort Dialog - ODRfast Design
 * Shows sort options for food items
 */
@Composable
fun SortDialog(
    currentSort: String?,
    onDismiss: () -> Unit,
    onSortSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                SortOption(
                    label = stringResource(R.string.sort_price_low_to_high),
                    value = "price:asc",
                    isSelected = currentSort == "price:asc",
                    onSelected = onSortSelected,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                SortOption(
                    label = stringResource(R.string.sort_price_high_to_low),
                    value = "price:desc",
                    isSelected = currentSort == "price:desc",
                    onSelected = onSortSelected,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                SortOption(
                    label = stringResource(R.string.sort_name_asc),
                    value = "name:asc",
                    isSelected = currentSort == "name:asc",
                    onSelected = onSortSelected,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                SortOption(
                    label = stringResource(R.string.sort_name_desc),
                    value = "name:desc",
                    isSelected = currentSort == "name:desc",
                    onSelected = onSortSelected,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

/**
 * Single sort option row
 */
@Composable
private fun SortOption(
    label: String,
    value: String,
    isSelected: Boolean,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(value) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.cd_selected),
                tint = Color(0xFF4CAF50), // Green checkmark
            )
        }
    }
}

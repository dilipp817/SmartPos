package com.autobill.smartpos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Reusable Input Components for SmartPos
 * Tablet-optimized with proper sizing and keyboard handling
 */

/**
 * Standard Text Field
 * Used for regular text input
 * 
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Field label
 * @param modifier Modifier for styling
 * @param placeholder Placeholder text
 * @param enabled Whether field is enabled
 * @param keyboardType Type of keyboard to show
 * @param imeAction IME action button type
 */
@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    errorMessage: String? = null,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            singleLine = true,
            isError = errorMessage != null,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                errorTextColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        // Error message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Search Bar - Specialized input for search
 * Shows search icon and clear button
 * 
 * @param query Current search query
 * @param onQueryChange Callback when query changes
 * @param onSearch Callback when search is triggered
 * @param onClear Callback to clear search
 * @param modifier Modifier for styling
 * @param placeholder Placeholder text
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: () -> Unit = { onQueryChange("") },
    placeholder: String = "Search...",
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() },
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

/**
 * Number Input Field - For numeric input
 * 
 * @param value Current numeric value
 * @param onValueChange Callback when value changes
 * @param label Field label
 * @param modifier Modifier for styling
 * @param placeholder Placeholder text
 * @param enabled Whether field is enabled
 */
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0",
    enabled: Boolean = true,
    errorMessage: String? = null,
) {
    TextInputField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
        errorMessage = errorMessage,
    )
}

/**
 * Price Input Field - For monetary input
 * 
 * @param value Current price value (in rupees)
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for styling
 * @param enabled Whether field is enabled
 */
@Composable
fun PriceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorMessage: String? = null,
) {
    TextInputField(
        value = value,
        onValueChange = { newValue ->
            // Allow numbers and one decimal point
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val parts = filtered.split('.')
            if (parts.size <= 2) {
                onValueChange(filtered)
            }
        },
        label = "Price (₹)",
        modifier = modifier,
        placeholder = "0.00",
        enabled = enabled,
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Next,
        errorMessage = errorMessage,
    )
}


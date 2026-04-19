package com.autobill.smartpos.feature.admin.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.feature.admin.R

/**
 * Reusable dialog for creating or editing a food item.
 * isEditMode = true when [initialState.name] is not blank (opened from edit).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodFormDialog(
    formState: FoodFormState,
    categories: List<Category>,
    isSaving: Boolean,
    isEditMode: Boolean,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onVegetarianChange: (Boolean) -> Unit,
    onSpicyChange: (Boolean) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onAllergensChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title  = { Text(if (isEditMode) stringResource(R.string.food_form_title_edit) else stringResource(R.string.food_form_title_add)) },
        text   = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Name
                OutlinedTextField(
                    value         = formState.name,
                    onValueChange = onNameChange,
                    label         = { Text(stringResource(R.string.food_form_field_name)) },
                    isError       = formState.hasNameError,
                    supportingText = if (formState.hasNameError) {
                        { Text(stringResource(R.string.validation_name_required)) }
                    } else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                // Price
                val priceErrorText: String? = when (formState.priceError) {
                    PriceValidationError.REQUIRED -> stringResource(R.string.validation_price_required)
                    PriceValidationError.INVALID  -> stringResource(R.string.validation_price_invalid)
                    PriceValidationError.NEGATIVE -> stringResource(R.string.validation_price_negative)
                    null                          -> null
                }
                OutlinedTextField(
                    value         = formState.price,
                    onValueChange = onPriceChange,
                    label         = { Text(stringResource(R.string.food_form_field_price)) },
                    isError       = formState.priceError != null,
                    supportingText = priceErrorText?.let { msg -> { Text(msg) } },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth(),
                )
                // Description
                OutlinedTextField(
                    value         = formState.description,
                    onValueChange = onDescriptionChange,
                    label         = { Text(stringResource(R.string.food_form_field_description)) },
                    minLines      = 2,
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                )
                // Category dropdown
                if (categories.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedCat = categories.find { it.id == formState.categoryId }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        OutlinedTextField(
                            value         = selectedCat?.name ?: stringResource(R.string.food_form_category_none),
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text(stringResource(R.string.food_form_field_category)) },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            DropdownMenuItem(
                                text    = { Text(stringResource(R.string.food_form_category_none)) },
                                onClick = { onCategoryChange(null); expanded = false },
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text    = { Text(cat.name) },
                                    onClick = { onCategoryChange(cat.id); expanded = false },
                                )
                            }
                        }
                    }
                }
                // Image URL
                OutlinedTextField(
                    value         = formState.imageUrl,
                    onValueChange = onImageUrlChange,
                    label         = { Text(stringResource(R.string.food_form_field_image_url)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                // Prep time + calories row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = formState.preparationTime,
                        onValueChange = onPrepTimeChange,
                        label         = { Text(stringResource(R.string.food_form_field_prep_time)) },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier      = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value         = formState.calories,
                        onValueChange = onCaloriesChange,
                        label         = { Text(stringResource(R.string.food_form_field_calories)) },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier      = Modifier.weight(1f),
                    )
                }
                // Allergens
                OutlinedTextField(
                    value         = formState.allergens,
                    onValueChange = onAllergensChange,
                    label         = { Text(stringResource(R.string.food_form_field_allergens)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                // Toggle row
                @Composable
                fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(label, modifier = Modifier.weight(1f))
                        Switch(checked = checked, onCheckedChange = onCheckedChange)
                    }
                }
                ToggleRow(stringResource(R.string.food_form_toggle_vegetarian), formState.isVegetarian, onVegetarianChange)
                ToggleRow(stringResource(R.string.food_form_toggle_spicy),      formState.isSpicy,      onSpicyChange)
                if (isEditMode)
                    ToggleRow(stringResource(R.string.food_form_toggle_available), formState.isAvailable, onAvailableChange)

                Spacer(Modifier.height(4.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick  = onSave,
                enabled  = formState.isValid && !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator()
                else Text(if (isEditMode) stringResource(R.string.food_form_button_update) else stringResource(R.string.food_form_button_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.cancel)) }
        },
    )
}

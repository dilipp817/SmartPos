package com.autobill.smartpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable Card Components for SmartPos
 * Tablet-optimized with proper spacing and touch targets
 */

/**
 * Base Card - Foundation card component
 * Elevated surface with rounded corners
 * 
 * @param modifier Modifier for styling
 * @param backgroundColor Card background color
 * @param elevation Card elevation
 * @param content Card content
 */
@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        content()
    }
}

/**
 * Food Card - Display food item with image, name, price
 * Optimized for menu display
 * 
 * @param foodName Name of the food item
 * @param price Price of the food
 * @param restaurant Restaurant/Category
 * @param modifier Modifier for styling
 * @param onClick Callback when card is clicked
 */
@Composable
fun FoodCard(
    foodName: String,
    price: String,
    restaurant: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BaseCard(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Food info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = foodName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = restaurant,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // Right: Price
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Section Card - Display section with title and content
 * Used for grouping related items
 * 
 * @param title Section title
 * @param modifier Modifier for styling
 * @param content Section content
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )

            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

/**
 * Detail Card - Display detailed item information
 * Typically used for product details
 * 
 * @param title Card title
 * @param modifier Modifier for styling
 * @param content Card content
 */
@Composable
fun DetailCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
    ) {
        Column {
            // Header with title and background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                    .padding(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

/**
 * List Card - Display list item
 * Simple card for list display
 * 
 * @param label Item label
 * @param value Item value
 * @param modifier Modifier for styling
 */
@Composable
fun ListCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    BaseCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Empty State Card - Display when no data available
 * Useful for empty lists
 * 
 * @param message Message to display
 * @param modifier Modifier for styling
 * @param icon Optional icon content
 */
@Composable
fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    BaseCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


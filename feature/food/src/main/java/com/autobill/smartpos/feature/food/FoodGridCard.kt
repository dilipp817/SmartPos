package com.autobill.smartpos.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandOrange = Color(0xFFFC8019)

/**
 * Food Grid Card — ODRfast Design
 * Horizontal layout: image | name+price | quantity controls
 *
 * Quantity = 0 → shows orange "+" button
 * Quantity > 0 → shows "−  count  +" inline row
 */
@Composable
fun FoodGridCard(
    food: FoodItemUI,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = if (food.quantity > 0) 1.5.dp else 1.dp,
                color = if (food.quantity > 0) BrandOrange else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onAdd)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: Image placeholder
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (food.quantity > 0) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = food.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = if (food.quantity > 0) BrandOrange else Color(0xFFBDBDBD),
            )
        }

        // Center: Name + price
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = food.price,
                style = MaterialTheme.typography.labelLarge,
                color = BrandOrange,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Right: Quantity controls
        if (food.quantity > 0) {
            // Inline counter: −  2  +
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161),
                    )
                }
                Text(
                    text = "${food.quantity}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.width(20.dp),
                )
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BrandOrange),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        } else {
            // Add to cart button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BrandOrange)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to cart",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

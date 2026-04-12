package com.autobill.smartpos.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.autobill.smartpos.domain.model.Food

private val BrandOrange = Color(0xFFFC8019)
private val SurfaceGray = Color(0xFFF8F9FA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF666666)
private val VegGreen = Color(0xFF4CAF50)
private val DividerGray = Color(0xFFEAEAEA)

/**
 * Food Detail Screen — ODRfast Design
 *
 * Layout (Landscape Tablet):
 * ┌─────────────────────────┬──────────────────────────┐
 * │  LEFT PANEL (45%)       │  RIGHT PANEL (55%)        │
 * │  ─ Food image           │  ─ Name + category badge  │
 * │  ─ Veg/spicy badges     │  ─ Price                  │
 * │                         │  ─ Description            │
 * │                         │  ─ Prep time / calories   │
 * │                         │  ─ Allergens              │
 * │                         │  ─ Add to Cart button     │
 * └─────────────────────────┴──────────────────────────┘
 */
@Composable
fun FoodDetailScreen(
    food: Food,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceGray),
    ) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        FoodDetailTopBar(foodName = food.name, onBack = onBack)

        HorizontalDivider(color = DividerGray)

        // ── Body ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Left — Image + badges
            FoodDetailImagePanel(
                food = food,
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
            )

            // Right — Info + Add to Cart
            FoodDetailInfoPanel(
                food = food,
                cartQuantity = cartQuantity,
                onAddToCart = onAddToCart,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun FoodDetailTopBar(
    foodName: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
            )
        }
        Text(
            text = foodName,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Left Panel — Image ────────────────────────────────────────────────────────

@Composable
private fun FoodDetailImagePanel(
    food: Food,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Food image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                if (!food.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = food.name.take(2).uppercase(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Veg / Non-veg + Spicy badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VegBadge(isVegetarian = food.isVegetarian)
                if (food.isSpicy) SpicyBadge()
            }

            // Category chip
            val catName = food.categoryName
            if (!catName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = catName,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .border(1.dp, DividerGray, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// ── Right Panel — Info ────────────────────────────────────────────────────────

@Composable
private fun FoodDetailInfoPanel(
    food: Food,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Name
            Text(
                text = food.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price
            Text(
                text = "₹%.2f".format(food.price),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandOrange,
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray)
            Spacer(modifier = Modifier.height(12.dp))

            // Description
            val desc = food.description
            if (!desc.isNullOrBlank()) {
                Text(text = "Description", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick-info row: prep time + calories
            val hasQuickInfo = food.preparationTime != null || food.calories != null
            if (hasQuickInfo) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    food.preparationTime?.let {
                        InfoChip(label = "⏱ Prep time", value = "$it min")
                    }
                    food.calories?.let {
                        InfoChip(label = "🔥 Calories", value = "$it kcal")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Allergens
            val allergens = food.allergens
            if (!allergens.isNullOrBlank()) {
                Text(text = "⚠ Allergens", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = allergens, fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Availability
            if (!food.isAvailable) {
                Text(
                    text = "Currently unavailable",
                    fontSize = 13.sp,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = DividerGray)
            Spacer(modifier = Modifier.height(12.dp))

            // Add to Cart button
            AddToCartButton(
                cartQuantity = cartQuantity,
                isAvailable = food.isAvailable,
                onAddToCart = onAddToCart,
            )
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun VegBadge(isVegetarian: Boolean) {
    val color = if (isVegetarian) VegGreen else Color(0xFFF44336)
    val label = if (isVegetarian) "VEG" else "NON-VEG"
    Row(
        modifier = Modifier
            .border(1.5.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(50)),
        )
        Text(text = label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpicyBadge() {
    Row(
        modifier = Modifier
            .background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
                imageVector = Icons.Filled.Whatshot,
            contentDescription = "Spicy",
            tint = Color(0xFFF44336),
            modifier = Modifier.size(14.dp),
        )
        Text(text = "SPICY", fontSize = 11.sp, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(SurfaceGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun AddToCartButton(
    cartQuantity: Int,
    isAvailable: Boolean,
    onAddToCart: () -> Unit,
) {
    Button(
        onClick = onAddToCart,
        enabled = isAvailable,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandOrange,
            disabledContainerColor = Color(0xFFBBBBBB),
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (cartQuantity > 0) "Add More  ·  $cartQuantity in cart"
            else "Add to Cart",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Loading / Error states ────────────────────────────────────────────────────

@Composable
fun FoodDetailLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceGray),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = BrandOrange)
    }
}

@Composable
fun FoodDetailErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceGray),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Couldn't load food details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onBack) {
                    Text("Go Back", color = TextSecondary)
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Retry")
                }
            }
        }
    }
}








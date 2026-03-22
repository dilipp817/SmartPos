package com.autobill.smartpos.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Home Screen Container - ODRfast Design
 * Master-Detail layout optimized for landscape tablets
 * 
 * Layout (Landscape Tablet):
 * ┌──────────────────────────────────────────────────┐
 * │ HEADER (Tabs + Profile)                         │
 * ├───────────────────────────────┬─────────────────┤
 * │ SEARCH & FILTER (full width)  │                 │
 * ├───────────────────────────────┤  CART SIDEBAR   │
 * │ FOOD GRID (65%)               │  (35%)          │
 * │ (with Sort Button)            │                 │
 * │                               │                 │
 * └───────────────────────────────┴─────────────────┘
 */
@Composable
fun HomeScreen(
    data: HomeScreenData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
    ) {
        // Header Section
        HomeHeader(
            data = data.header,
        )

        // Main Content Row (Left: Menu | Right: Cart)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            // Left Panel: Search + Food Grid (65%)
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
            ) {
                // Search & Filter Section
                SearchFilterPanel(
                    data = data.searchFilter,
                )

                // Food Grid with Sort Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    FoodGridSection(
                        data = data.foodGrid,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Floating Sort Button (bottom center)
                    Button(
                        onClick = data.searchFilter.onSortClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE33E3E),
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(text = data.searchFilter.sortOption)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort",
                        )
                    }
                }
            }

            // Right Panel: Cart Summary (35%)
            CartSummaryFooter(
                data = data.cartSummary,
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight(),
            )
        }
    }
}


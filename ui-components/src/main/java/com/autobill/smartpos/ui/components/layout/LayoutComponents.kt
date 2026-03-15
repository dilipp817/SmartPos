package com.autobill.smartpos.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Responsive Grid - Adaptive grid that adjusts columns based on screen width
@Composable
fun <T> ResponsiveGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 8.dp,
    itemContent: @Composable (T) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(horizontalPadding),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(verticalPadding),
        contentPadding = PaddingValues(horizontalPadding),
    ) {
        items(items.size) { index ->
            itemContent(items[index])
        }
    }
}

// Tablet Split Layout - Split-view layout optimized for tablets
@Composable
fun TabletSplitLayout(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leftPanelWidth: Dp = 400.dp,
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left Panel
        Box(
            modifier = Modifier
                .width(leftPanelWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            leftContent()
        }

        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
        )

        // Right Panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            rightContent()
        }
    }
}

// Three Column Layout - For tablet with menu, order, and bill
@Composable
fun TripleColumnLayout(
    leftContent: @Composable () -> Unit,
    middleContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    columnWidth: Dp = 350.dp,
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left Column
        Box(
            modifier = Modifier
                .width(columnWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            leftContent()
        }

        Divider(Modifier.fillMaxHeight().width(1.dp))

        // Middle Column
        Box(
            modifier = Modifier
                .width(columnWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            middleContent()
        }

        Divider(Modifier.fillMaxHeight().width(1.dp))

        // Right Column
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            rightContent()
        }
    }
}

// Adaptive Scaffold - Main app scaffold with adaptive navigation
@Composable
fun SmartPosScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        content(padding)
    }
}

// Responsive Layout - Main responsive container
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (isTablet: Boolean) -> Unit,
) {
    val isTablet = isTabletDevice()
    Box(modifier = modifier.fillMaxSize()) {
        content(isTablet)
    }
}

// Bottom Sheet Layout - For modal bottom sheets
@Composable
fun BottomSheetLayout(
    sheetContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            mainContent()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
        ) {
            sheetContent()
        }
    }
}

// Vertical List Container - For scrollable vertical lists
@Composable
fun VerticalListLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        content()
    }
}

// Card Grid Layout - Grid of cards
@Composable
fun CardGridLayout(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        content()
    }
}

// Helper function to detect tablet device
@Composable
private fun isTabletDevice(): Boolean {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return screenWidthDp >= 600
}


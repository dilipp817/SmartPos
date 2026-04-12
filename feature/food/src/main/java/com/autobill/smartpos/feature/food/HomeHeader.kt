package com.autobill.smartpos.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Home Screen Header Component - ODRfast Design
 * Displays logo, tabs (Offline/Online orders), and business profile
 */
@Composable
fun HomeHeader(
    data: HeaderData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Top Row: Logo + Tabs + Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left: Logo
            Text(
                text = data.appTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )

            // Center: Tab Switcher
            TabRow(
                selectedTabIndex = if (data.selectedTab == OrderTab.OFFLINE) 0 else 1,
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (data.selectedTab == OrderTab.OFFLINE) 0 else 1]),
                        color = Color(0xFFE33E3E),
                        height = 3.dp,
                    )
                },
                divider = {},
            ) {
                Tab(
                    selected = data.selectedTab == OrderTab.OFFLINE,
                    onClick = { data.onTabChange(OrderTab.OFFLINE) },
                    text = {
                        Text(
                            text = "Offline orders",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (data.selectedTab == OrderTab.OFFLINE) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    selectedContentColor = Color(0xFFE33E3E),
                    unselectedContentColor = Color(0xFF757575),
                )
                Tab(
                    selected = data.selectedTab == OrderTab.ONLINE,
                    onClick = { data.onTabChange(OrderTab.ONLINE) },
                    text = {
                        Text(
                            text = "Online orders",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (data.selectedTab == OrderTab.ONLINE) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    selectedContentColor = Color(0xFFE33E3E),
                    unselectedContentColor = Color(0xFF757575),
                )
            }

            // Right: Manage Menu chip (admin / super_admin only) + Business Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // "Manage Menu" chip — only shown for admin / super_admin
                if (data.canManageMenu) {
                    Surface(
                        onClick = data.onManageMenuClick,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Manage Menu",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Business Profile button
                Row(
                    modifier = Modifier
                        .clickable(onClick = data.onProfileClick)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = data.businessName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF212121),
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF757575),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF757575),
                    )
                }
            }
        }

        // Bottom Divider
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
    }
}


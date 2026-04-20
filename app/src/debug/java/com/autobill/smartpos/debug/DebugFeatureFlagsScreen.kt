package com.autobill.smartpos.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DebugFeatureFlagsScreen(
    viewModel: FeatureFlagsViewModel,
    onBack: () -> Unit,
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚩 Feature Flags",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    text = "DEBUG BUILD ONLY — overrides persist across restarts",
                    color = Color(0xFFFFAB40),
                    fontSize = 11.sp,
                )
            }
            OutlinedButton(
                onClick = viewModel::clearAll,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text("Clear All", fontSize = 12.sp)
            }
        }

        // ── Priority legend ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SourceChip("OVERRIDE", Color(0xFFFF6D00), Color.White)
            SourceChip("REMOTE", Color(0xFF1565C0), Color.White)
            SourceChip("DEFAULT", Color(0xFF424242), Color(0xFFBDBDBD))
        }

        HorizontalDivider(color = Color(0xFF333333))

        // ── Flag rows ────────────────────────────────────────────────────────
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows, key = { it.flag.name }) { row ->
                FlagRow(
                    row = row,
                    onToggle = { enabled -> viewModel.toggle(row.flag, enabled) },
                    onClearOverride = { viewModel.clearOverride(row.flag) },
                )
                HorizontalDivider(color = Color(0xFF2A2A2A))
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun FlagRow(
    row: FlagRowState,
    onToggle: (Boolean) -> Unit,
    onClearOverride: () -> Unit,
) {
    val source = when {
        row.override != null -> "OVERRIDE"
        row.hasRemoteValue   -> "REMOTE"
        else                 -> "DEFAULT"
    }
    val sourceColor = when (source) {
        "OVERRIDE" -> Color(0xFFFF6D00)
        "REMOTE"   -> Color(0xFF1565C0)
        else       -> Color(0xFF424242)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = row.flag.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                )
                SourceChip(
                    label = source,
                    background = sourceColor,
                    content = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = row.flag.description,
                color = Color(0xFF9E9E9E),
                fontSize = 11.sp,
            )
            if (row.override != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onClearOverride,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF37474F),
                        contentColor = Color(0xFFFFAB40),
                    ),
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 10.dp, vertical = 0.dp
                    ),
                ) {
                    Text("Clear override", fontSize = 11.sp)
                }
            }
        }

        Switch(
            checked = row.resolvedValue,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF69F0AE),
                checkedTrackColor = Color(0xFF1B5E20),
                uncheckedThumbColor = Color(0xFFFF5252),
                uncheckedTrackColor = Color(0xFF4E0000),
            ),
        )
    }
}

@Composable
private fun SourceChip(label: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, color = content, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace)
    }
}


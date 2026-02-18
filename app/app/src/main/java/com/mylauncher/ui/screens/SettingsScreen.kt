package com.mylauncher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylauncher.data.model.AccentColor

/**
 * Settings screen — theme, accent color, grid layout, tile opacity, animation frequency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentAccentColor: AccentColor,
    isDarkTheme: Boolean,
    gridColumns: Int,
    tileOpacity: Float,
    animationIntervalMs: Long,
    onAccentColorChanged: (AccentColor) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onGridColumnsChanged: (Int) -> Unit,
    onTileOpacityChanged: (Float) -> Unit,
    onAnimationIntervalChanged: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Text(
                text = "Settings",
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme toggle
            item {
                SettingsSection(title = "Theme", textColor = textColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dark theme", color = textColor)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onDarkThemeChanged,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = currentAccentColor.color
                            )
                        )
                    }
                }
            }

            // Accent color picker
            item {
                SettingsSection(title = "Accent Color", textColor = textColor) {
                    // Show color grid — 4 columns
                    val colors = AccentColor.entries
                    val rows = colors.chunked(4)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in rows) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (accent in row) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(accent.color)
                                            .then(
                                                if (accent == currentAccentColor) {
                                                    Modifier.border(
                                                        width = 3.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                } else Modifier
                                            )
                                            .clickable { onAccentColorChanged(accent) }
                                    ) {
                                        if (accent == currentAccentColor) {
                                            Text(
                                                text = "✓",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentAccentColor.displayName,
                            color = currentAccentColor.color,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Grid columns
            item {
                SettingsSection(title = "Grid Columns", textColor = textColor) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cols in listOf(3, 4, 6)) {
                            FilterChip(
                                selected = gridColumns == cols,
                                onClick = { onGridColumnsChanged(cols) },
                                label = { Text("$cols") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentAccentColor.color,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Tile opacity
            item {
                SettingsSection(title = "Tile Transparency", textColor = textColor) {
                    Column {
                        Text(
                            text = "${(tileOpacity * 100).toInt()}% opaque",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Slider(
                            value = tileOpacity,
                            onValueChange = onTileOpacityChanged,
                            valueRange = 0f..1f,
                            steps = 19,
                            colors = SliderDefaults.colors(
                                thumbColor = currentAccentColor.color,
                                activeTrackColor = currentAccentColor.color
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transparent", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                            Text("Opaque", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tile animation interval
            item {
                SettingsSection(title = "Live Tile Animation", textColor = textColor) {
                    val intervals = listOf(
                        3000L to "3s",
                        5000L to "5s",
                        10000L to "10s",
                        30000L to "30s",
                        0L to "Off"
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((interval, label) in intervals) {
                            FilterChip(
                                selected = animationIntervalMs == interval,
                                onClick = { onAnimationIntervalChanged(interval) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentAccentColor.color,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // About
            item {
                SettingsSection(title = "About", textColor = textColor) {
                    Column {
                        Text("My Launcher", color = textColor, fontWeight = FontWeight.Medium)
                        Text(
                            "Version 0.1.0-alpha",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "A Windows Phone Metro-inspired launcher for Android.",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bottom spacer
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            color = textColor.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

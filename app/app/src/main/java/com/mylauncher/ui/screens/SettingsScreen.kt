package com.mylauncher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylauncher.data.model.SavedTheme
import com.mylauncher.data.model.toComposeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings screen — accent color picker, tile opacity, animation frequency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentAccentColor: Color,
    accentColorArgb: Long,
    tileOpacity: Float,
    animationIntervalMs: Long,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    darkModeEnabled: Boolean,
    savedThemes: List<SavedTheme>,
    onAccentColorChanged: (Long) -> Unit,
    onTileOpacityChanged: (Float) -> Unit,
    onAnimationIntervalChanged: (Long) -> Unit,
    onBevelEnabledChanged: (Boolean) -> Unit,
    onBevelDepthChanged: (Float) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onSaveTheme: (String) -> Unit,
    onApplyTheme: (SavedTheme) -> Unit,
    onDeleteTheme: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = darkModeEnabled
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
            // Accent color picker
            item {
                SettingsSection(title = "Accent Color", textColor = textColor) {
                    HsvColorPicker(
                        currentColor = currentAccentColor,
                        onColorSelected = { color ->
                            val argb = (
                                ((color.alpha * 255).toLong() shl 24) or
                                ((color.red * 255).toLong() shl 16) or
                                ((color.green * 255).toLong() shl 8) or
                                (color.blue * 255).toLong()
                            )
                            onAccentColorChanged(argb)
                        }
                    )
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
                                thumbColor = currentAccentColor,
                                activeTrackColor = currentAccentColor
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

            // Tile bevel / glass effect
            item {
                SettingsSection(title = "Tile Bevel", textColor = textColor) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Glass-like bevel edges",
                                color = textColor,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = bevelEnabled,
                                onCheckedChange = onBevelEnabledChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = currentAccentColor,
                                    checkedTrackColor = currentAccentColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                        if (bevelEnabled) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Depth: ${String.format(java.util.Locale.US, "%.1f", bevelDepth)}×",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Slider(
                                value = bevelDepth,
                                onValueChange = onBevelDepthChanged,
                                valueRange = 0.2f..3f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = currentAccentColor,
                                    activeTrackColor = currentAccentColor
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtle", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("Deep", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Tile animation interval
            item {
                SettingsSection(title = "Dark Mode", textColor = textColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Dark theme for Settings & All Apps",
                            color = textColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = onDarkModeChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentAccentColor,
                                checkedTrackColor = currentAccentColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Live tile animation interval
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
                                    selectedContainerColor = currentAccentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Save current layout as theme
            item {
                SettingsSection(title = "Themes", textColor = textColor) {
                    var themeName by remember { mutableStateOf("") }
                    var showSaveField by remember { mutableStateOf(false) }

                    if (!showSaveField) {
                        OutlinedButton(
                            onClick = { showSaveField = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = currentAccentColor
                            ),
                            border = BorderStroke(1.dp, currentAccentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Current Layout as Theme")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = themeName,
                                onValueChange = { themeName = it },
                                label = { Text("Theme name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentAccentColor,
                                    cursorColor = currentAccentColor,
                                    focusedLabelColor = currentAccentColor,
                                    unfocusedTextColor = textColor,
                                    focusedTextColor = textColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (themeName.isNotBlank()) {
                                            onSaveTheme(themeName.trim())
                                            themeName = ""
                                            showSaveField = false
                                        }
                                    },
                                    enabled = themeName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = currentAccentColor
                                    )
                                ) {
                                    Text("Save", color = Color.White)
                                }
                                TextButton(onClick = {
                                    showSaveField = false
                                    themeName = ""
                                }) {
                                    Text("Cancel", color = textColor.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // Saved themes list
            if (savedThemes.isNotEmpty()) {
                item {
                    Text(
                        text = "SAVED THEMES",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(savedThemes, key = { it.id }) { theme ->
                    SavedThemeCard(
                        theme = theme,
                        textColor = textColor,
                        onApply = { onApplyTheme(theme) },
                        onDelete = { onDeleteTheme(theme.id) }
                    )
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
// ─────────────────── Saved Theme Card ───────────────────

@Composable
private fun SavedThemeCard(
    theme: SavedTheme,
    textColor: Color,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColor = theme.accentColorArgb.toComposeColor()
    val dateStr = remember(theme.createdAt) {
        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            .format(Date(theme.createdAt))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = textColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
        ) {
            // Accent color swatch
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(themeColor)
            )

            Spacer(Modifier.width(12.dp))

            // Theme info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${theme.tiles.size} tiles • $dateStr",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            // Apply button
            IconButton(onClick = onApply) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Apply theme",
                    tint = themeColor
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete theme",
                    tint = textColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
// ─────────────────────── HSV Color Picker ───────────────────────

/**
 * A simple HSV color picker: a hue bar + a saturation/value gradient box.
 */
@Composable
private fun HsvColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit
) {
    // Decompose current color into HSV
    val initialHsv = remember(currentColor) {
        val r = currentColor.red
        val g = currentColor.green
        val b = currentColor.blue
        rgbToHsv(r, g, b)
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val selectedColor = remember(hue, saturation, value) {
        hsvToColor(hue, saturation, value)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Preview swatch
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(selectedColor)
        ) {
            Text(
                text = String.format("#%06X", 0xFFFFFF and selectedColor.toArgbInt()),
                color = if (value > 0.5f && saturation < 0.5f) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Saturation-Value gradient box
        Text(
            text = "SHADE",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, hsvToColor(hue, 1f, 1f))
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .pointerInput(hue) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pos = event.changes.firstOrNull()?.position ?: continue
                            event.changes.forEach { it.consume() }
                            val s = (pos.x / size.width).coerceIn(0f, 1f)
                            val v = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                            saturation = s
                            value = v
                            onColorSelected(hsvToColor(hue, s, v))
                        }
                    }
                }
        ) {
            // Thumb indicator
            val thumbX = saturation * maxWidth.value
            val thumbY = (1f - value) * maxHeight.value
            Box(
                modifier = Modifier
                    .offset(x = (thumbX - 8).dp, y = (thumbY - 8).dp)
                    .size(16.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(50))
                    .background(selectedColor, RoundedCornerShape(50))
            )
        }

        // Hue slider
        Text(
            text = "HUE",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = (0..360 step 30).map { h ->
                            hsvToColor(h.toFloat(), 1f, 1f)
                        }
                    )
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pos = event.changes.firstOrNull()?.position ?: continue
                            event.changes.forEach { it.consume() }
                            val h = (pos.x / size.width).coerceIn(0f, 1f) * 360f
                            hue = h
                            onColorSelected(hsvToColor(h, saturation, value))
                        }
                    }
                }
        ) {
            // Hue thumb
            val thumbX = (hue / 360f) * maxWidth.value
            Box(
                modifier = Modifier
                    .offset(x = (thumbX - 8).dp, y = 0.dp)
                    .fillMaxHeight()
                    .width(16.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
}

// ─────────────────────── Color Helpers ───────────────────────

private fun rgbToHsv(r: Float, g: Float, b: Float): FloatArray {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6)
        max == g -> 60f * (((b - r) / delta) + 2)
        else -> 60f * (((r - g) / delta) + 4)
    }.let { if (it < 0) it + 360f else it }

    val s = if (max == 0f) 0f else delta / max
    val v = max

    return floatArrayOf(h, s, v)
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
    val m = v - c
    val (r, g, b) = when {
        h < 60 -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}

private fun Color.toArgbInt(): Int {
    return ((alpha * 255).toInt() shl 24) or
           ((red * 255).toInt() shl 16) or
           ((green * 255).toInt() shl 8) or
           (blue * 255).toInt()
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

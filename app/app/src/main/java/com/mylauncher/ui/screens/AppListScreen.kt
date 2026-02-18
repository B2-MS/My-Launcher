package com.mylauncher.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.mylauncher.data.model.AccentColor
import com.mylauncher.data.model.AppInfo

/**
 * The App List screen — alphabetically grouped list of installed apps.
 * Accessed by swiping right or tapping "All Apps →" from the Start screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    apps: List<AppInfo>,
    accentColor: AccentColor,
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    // Group by first letter
    val groupedApps = remember(filteredApps) {
        filteredApps.groupBy { app ->
            val firstChar = app.appName.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar.isLetter()) firstChar.toString() else "#"
        }.toSortedMap()
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF5F5F5)
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search Apps",
                    color = textColor.copy(alpha = 0.5f)
                )
            },
            leadingIcon = null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textColor.copy(alpha = 0.7f)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = textColor.copy(alpha = 0.5f),
                unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                cursorColor = accentColor.color
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // App list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            groupedApps.forEach { (letter, appsInGroup) ->
                // Letter header
                item(key = "header_$letter") {
                    Text(
                        text = letter,
                        color = textColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 16.dp,
                            bottom = 4.dp
                        )
                    )
                }

                // Apps in this letter group
                items(
                    items = appsInGroup,
                    key = { "app_${it.packageName}" }
                ) { app ->
                    AppListItem(
                        app = app,
                        accentColor = accentColor.color,
                        textColor = textColor,
                        onTap = { onAppTap(app.packageName) },
                        onLongPress = { onAppLongPress(app) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppListItem(
    app: AppInfo,
    accentColor: Color,
    textColor: Color,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Accent-colored square with app icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        ) {
            if (app.icon != null) {
                val bitmap = remember(app.icon) {
                    app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.appName,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // App name
        Text(
            text = app.appName,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1
        )
    }
}

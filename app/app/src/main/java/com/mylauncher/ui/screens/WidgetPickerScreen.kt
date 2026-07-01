package com.mylauncher.ui.screens

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

/**
 * A picker screen showing all available App Widgets grouped by app.
 * User taps a widget to select it, which triggers the bind flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPickerScreen(
    providers: List<AppWidgetProviderInfo>,
    accentColor: Color,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Group by app label
    val grouped = remember(providers) {
        providers
            .sortedBy { it.loadLabel(pm).toString() }
            .groupBy {
                it.provider.packageName
            }
            .toSortedMap(compareBy { key ->
                providers.find { it.provider.packageName == key }
                    ?.loadLabel(pm)?.toString() ?: key
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Widget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black.copy(alpha = 0.95f),
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            grouped.forEach { (packageName, widgets) ->
                // App header
                item(key = "header_$packageName") {
                    val appLabel = try {
                        pm.getApplicationLabel(
                            pm.getApplicationInfo(packageName, 0)
                        ).toString()
                    } catch (_: Exception) { packageName }

                    Text(
                        text = appLabel,
                        color = accentColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                // Widget entries
                items(widgets, key = { it.provider.flattenToString() }) { info ->
                    WidgetPickerItem(
                        info = info,
                        onClick = { onWidgetSelected(info) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerItem(
    info: AppWidgetProviderInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(info) { info.loadLabel(pm)?.toString() ?: "Widget" }
    val previewDrawable: Drawable? = remember(info) {
        try { info.loadPreviewImage(context, 0) } catch (_: Exception) { null }
            ?: try { info.loadIcon(context, 0) } catch (_: Exception) { null }
    }
    val minWidth = info.minWidth
    val minHeight = info.minHeight

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            if (previewDrawable != null) {
                val bitmap = remember(previewDrawable) {
                    previewDrawable.toBitmap(64, 64).asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = label,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .then(Modifier)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${minWidth}×${minHeight} dp",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

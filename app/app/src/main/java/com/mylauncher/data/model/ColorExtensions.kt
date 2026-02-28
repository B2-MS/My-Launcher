package com.mylauncher.data.model

import androidx.compose.ui.graphics.Color

/** Convert a stored ARGB Long to a Compose [Color]. */
fun Long.toComposeColor(): Color = Color(this.toInt())

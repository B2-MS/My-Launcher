package com.mylauncher.data.model

import androidx.compose.ui.graphics.Color

/**
 * Windows Phone accent color palette.
 */
enum class AccentColor(val displayName: String, val color: Color) {
    COBALT("Cobalt", Color(0xFF0050EF)),
    CYAN("Cyan", Color(0xFF1BA1E2)),
    TEAL("Teal", Color(0xFF00ABA9)),
    EMERALD("Emerald", Color(0xFF008A00)),
    LIME("Lime", Color(0xFFA4C400)),
    YELLOW("Yellow", Color(0xFFE3C800)),
    AMBER("Amber", Color(0xFFF0A30A)),
    MANGO("Mango", Color(0xFFF09609)),
    ORANGE("Orange", Color(0xFFFA6800)),
    CRIMSON("Crimson", Color(0xFFA20025)),
    RED("Red", Color(0xFFE51400)),
    MAGENTA("Magenta", Color(0xFFD80073)),
    MAUVE("Mauve", Color(0xFF76608A)),
    STEEL("Steel", Color(0xFF647687)),
    INDIGO("Indigo", Color(0xFF6A00FF)),
    VIOLET("Violet", Color(0xFFAA00FF))
}

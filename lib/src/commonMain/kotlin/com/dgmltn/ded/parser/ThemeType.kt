package com.dgmltn.ded.parser

import androidx.compose.ui.graphics.Color
import com.dgmltn.ded.hexToColor

// Theme textmate files: https://github.com/filmgirl/TextMate-Themes

enum class ThemeType(
    val resLocation: String,
    val defaultFg: Color,
    val defaultBg: Color,
    val cursor: Color,
    val selection: Color,
    val guide: Color = defaultFg.copy(alpha = 0.5f),
) {
    Abyss(
        resLocation = "files/Abyss.tmTheme",
        defaultFg = "#6688cc".hexToColor(),
        defaultBg = "#000c18".hexToColor(),
        cursor = "#ddbb88".hexToColor(),
        selection = "#770811".hexToColor(),
    ),
    Bespin(
        resLocation = "files/Bespin.tmTheme",
        defaultFg = "#BAAE9E".hexToColor(),
        defaultBg = "#28211C".hexToColor(),
        cursor = "#A7A7A7".hexToColor(),
        selection = "#DDF0FF33".hexToColor(),
    ),
    MadeOfCode(
        resLocation = "files/MadeOfCode.tmTheme",
        defaultFg = "#F8F8F8".hexToColor(),
        defaultBg = "#090A1BF2".hexToColor(),
        cursor = "#00FFFF".hexToColor(),
        selection = "#007DFF80".hexToColor(),
    )
}
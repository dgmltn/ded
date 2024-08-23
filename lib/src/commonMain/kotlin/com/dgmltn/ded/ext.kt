package com.dgmltn.ded

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue


fun IntProgression.toIntRange(): IntRange {
    if (step.absoluteValue != 1) throw IllegalArgumentException("Only step 1 is supported")
    return if (first <= last) first..last else last .. first
}


fun String.hexToColor(): Color {
    require(this.startsWith("#")) { "Hex color string must start with '#'." }
    val hexString = this.substring(1) // Remove the '#' prefix

    if (hexString.length == 3) {
        val red = hexString[0].digitToInt(16) * 17
        val green = hexString[1].digitToInt(16) * 17
        val blue = hexString[2].digitToInt(16) * 17
        return Color(red, green, blue)
    } else if (hexString.length == 6) {
        val red = hexString.substring(0, 2).toInt(16)
        val green = hexString.substring(2, 4).toInt(16)
        val blue = hexString.substring(4, 6).toInt(16)
        return Color(red, green, blue)
    } else if (hexString.length == 8) {
        // Eight-digit notation (with alpha)
        val red = hexString.substring(0, 2).toInt(16)
        val green = hexString.substring(2, 4).toInt(16)
        val blue = hexString.substring(4, 6).toInt(16)
        val alpha = hexString.substring(6, 8).toInt(16)
        return Color(red, green, blue, alpha)
    } else {
        throw IllegalArgumentException("Invalid hex color string length: $hexString")
    }
}
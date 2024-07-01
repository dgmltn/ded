package com.dgmltn.ded.theme

import androidx.compose.ui.graphics.Color


data class DedColors(
    val canvas: Color,
    val cursor: Color,
    val lineNumber: Color,
    val selectionBg: Color,
    val selectionFg: Color,
    // Syntax highlighting:
    val code: Color,
    val keyword: Color,
    val string: Color,
    val literal: Color,
    val comment: Color,
    val metadata: Color,
    val multilineComment: Color,
    val punctuation: Color,
    val mark: Color
)
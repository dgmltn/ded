package com.dgmltn.ded.theme

import androidx.compose.ui.graphics.Color


data class DedColors(
    val canvas: Color,
    val cursor: Color,
    val lineNumber: Color,
    val text: Color,
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
) {
    companion object {
        val Default = DedColors(
            canvas = Color(0xFF2B2B2B),
            cursor = Color(0xFFD0D0D0),
            lineNumber = Color(0xFF5A5A5A),
            text = Color(0xFFD0D0D0),
            selectionBg = Color(0xFF264F78),
            selectionFg = Color(0xFFD0D0D0),
            code = Color(0xFFF8F8F2),
            comment = Color(0xFFFD971F),
            keyword = Color(0xFFF92672),
            literal = Color(0xFFAE81FF),
            mark = Color(0xFFF8F8F2),
            metadata = Color(0xFFB8F4B8),
            multilineComment = Color(0xFFFD971F),
            punctuation = Color(0xFFF8F8F2),
            string = Color(0xFFE6DB74),
        )
    }
}
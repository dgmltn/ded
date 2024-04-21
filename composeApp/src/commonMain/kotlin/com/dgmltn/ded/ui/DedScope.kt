package com.dgmltn.ded.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

interface DedScope {
    val textMeasurer: TextMeasurer
    val cellSize: IntSize

    @Composable
    fun Modifier.position(row: Int, col: Int) = this.then(
        with(LocalDensity.current) {
            offset { IntOffset(cellSize.width * col, cellSize.height * row) }
                .size(width = cellSize.width.toDp(), height = cellSize.height.toDp())
        }
    )

    @Composable
    fun Cursor(
        row: Int,
        col: Int,
        color: Color,
    ) {
        Box(
            modifier = Modifier
                .position(row, col)
                .background(color)
        )
    }

    @Composable
    fun LineNumber(
        row: Int,
        color: Color,
    ) {
        // row+1 here because humans are used to 1-based line numbers
        CellGlyphs(row, 0, (row + 1).toString(), color)
    }

    @Composable
    fun CellGlyph(
        row: Int,
        col: Int,
        glyph: Char,
        color: Color,
    ) {
        Text(
            text = glyph.toString(),
            color = color,
            modifier = Modifier
                .position(row, col)
        )
    }

    @Composable
    fun CellGlyphs(
        row: Int,
        col: Int,
        glyphs: String,
        color: Color,
    ) {
        glyphs.toCharArray().forEachIndexed { index, c ->
            CellGlyph(
                row = row,
                col = col + index,
                glyph = c,
                color = color,
            )
        }
    }
}

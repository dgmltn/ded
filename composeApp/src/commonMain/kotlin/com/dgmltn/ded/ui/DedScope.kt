package com.dgmltn.ded.ui

import DedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

private val END = Char(0)
private const val NEWLINE = '\n'

interface DedScope {
    val cellSize: IntSize
    val colors: DedColors
    val cellOffset: IntOffset

    @Composable
    fun Modifier.position(row: Int, col: Int) = this.then(
        with(LocalDensity.current) {
            offset { IntOffset(cellSize.width * col, cellSize.height * row) }
                .size(width = cellSize.width.toDp(), height = cellSize.height.toDp())
        }
    )

    @Composable
    fun Cursor(row: Int, col: Int) {
        Box(
            modifier = Modifier
                .position(row, col)
                .background(colors.cursor)
        )
    }

    @Composable
    fun LineNumberGlyphs(row: Int) {
        // row+1 here because humans are used to 1-based line numbers
        CellGlyphs(row, 0, (row + 1).toString(), colors.lineNumber)
    }

    @Composable
    fun BodyGlyph(row: Int, col: Int, glyph: Char) {
        CellGlyph(row + cellOffset.y, col + cellOffset.x, glyph, colors.text)
    }

    @Composable
    fun BodyGlyphs(row: Int, col: Int, glyphs: String) {
        CellGlyphs(row + cellOffset.y, col + cellOffset.x, glyphs, colors.text)
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

    @Composable
    fun AllVisibleGlyphs(
        length: Int,
        getCharAt: (Int) -> Char,
        cursorPos: Int,
    ) {
        var row = 0
        var col = 0

        (0..length).forEach { i ->
            val c = if (i == length) END else getCharAt(i)

            // Draw line number
            if (col == 0) {
                LineNumberGlyphs(row)
            }

            // Draw glyph (if it's drawable)
            if (c != NEWLINE && c != END) {
                BodyGlyph(row, col, c)
            }

            // Draw cursor
            if (i == cursorPos) {
                Cursor(
                    row = row + cellOffset.y,
                    col = col + cellOffset.x,
                )
            }

            // Update col, row
            if (c == NEWLINE) {
                row++
                col = 0
            } else {
                col++
            }
        }
    }
}

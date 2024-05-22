package com.dgmltn.ded.ui

import DedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
    fun LineNumberGlyphs(row: Int) {
        // row+1 here because humans are used to 1-based line numbers
        CellGlyphs(row, 0, (row + 1).toString(), colors.lineNumber)
    }

    @Composable
    fun BodyGlyph(row: Int, col: Int, glyph: Char, fgColor: Color, bgColor: Color?) {
        CellGlyph(
            row = row + cellOffset.y,
            col = col + cellOffset.x,
            glyph = glyph,
            fgColor = fgColor,
            bgColor = bgColor
        )
    }

    @Composable
    fun CellGlyph(
        row: Int,
        col: Int,
        glyph: Char,
        fgColor: Color,
        bgColor: Color?,
    ) {
        if (bgColor != null) {
            Box(
                modifier = Modifier
                    .position(row, col)
                    .background(bgColor)
            ) {
                if (glyph != END && glyph != NEWLINE) {
                    Text(
                        text = glyph.toString(),
                        color = fgColor,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
        else if (glyph != END && glyph != NEWLINE) {
            Text(
                text = glyph.toString(),
                color = fgColor,
                modifier = Modifier
                    .position(row, col)
            )
        }
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
                fgColor = color,
                bgColor = null
            )
        }
    }

    @Composable
    fun AllVisibleGlyphs(
        length: Int,
        getCharAt: (Int) -> Char,
        cursorPos: Int,
        selection: IntProgression?,
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
            if (i == cursorPos) {
                BodyGlyph(
                    row = row,
                    col = col,
                    glyph = c,
                    fgColor = colors.text,
                    bgColor = colors.cursor,
                )
            }
            else if (selection?.contains(i) == true) {
                BodyGlyph(row, col, c, colors.selectionFg, colors.selectionBg)
            }
            else {
                BodyGlyph(row, col, c, colors.text, null)
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

package com.dgmltn.ded.ui

import DedColors
import LocalDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import co.touchlab.kermit.Logger
import com.dgmltn.ded.numDigits

private const val GLYPH = "W"
private val END = Char(0)
private val NEWLINE = '\n'

@Composable
fun DedGrid(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
    textStyle: TextStyle = LocalTextStyle.current,
    colors: DedColors = LocalDefaults.current.editor
) {
    val lineNumberXOffset = state.lineCount.numDigits() + 1

    Box(
        modifier = modifier
            .onSizeChanged { state.windowSizePx = it },
    ) {
        InternalDedGrid(
            textStyle = textStyle
        ) {
            var row = 0
            var col = 0
            LineNumber(
                row = row,
                color = colors.lineNumber
            )
            (0 .. state.length).forEach { i ->
                val c = if (i == state.length) END else state.getCharAt(i)
                Logger.e { "DOUG: $c ($row, $col)" }
                if (c != NEWLINE && c != END) {
                    CellGlyph(row, col + lineNumberXOffset, c, colors.text)
                }
                if (i == state.cursorPos) {
                    Cursor(
                        row = row,
                        col = col + lineNumberXOffset,
                        color = colors.cursor
                    )
                }
                col++
                if (c == NEWLINE) {
                    row++
                    col = 0
                    LineNumber(
                        row = row,
                        color = colors.lineNumber
                    )
                }
            }

        }
    }
}

@Composable
private fun InternalDedGrid(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    content: @Composable DedScope.() -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var cellSize by remember { mutableStateOf(IntSize(16, 24)) }

    LaunchedEffect(GLYPH) {
        val result = GLYPH.let { textMeasurer.measure(GLYPH, textStyle) }
        textLayoutResult = result
        cellSize = result.size
    }

    val scope = remember {
        object : DedScope {}
    }

    Layout(
        modifier = modifier,
        content = {
            CompositionLocalProvider(
                LocalTextStyle provides textStyle,
            ) {
                content(scope)
            }
        }
    ) { measurables, constraints ->

        val cellConstraints = constraints.copy(
            minWidth = cellSize.width,
            maxWidth = cellSize.width,
            minHeight = cellSize.height,
            maxHeight = cellSize.height
        )

        val placeables = measurables.map { it.measure(cellConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            Logger.e { "DOUG: placing ${placeables.size} placeables" }
            placeables.forEach { placeable ->
                // Position item on the screen
                (placeable.parentData as? DedChildDataNode)?.let {
                    placeable.place(
                        x = it.col * cellSize.width,
                        y = it.row * cellSize.height
                    )
                }
            }
        }
    }
}

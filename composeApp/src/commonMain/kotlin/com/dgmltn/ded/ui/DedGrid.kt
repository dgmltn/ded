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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import com.dgmltn.ded.div
import com.dgmltn.ded.numDigits

private const val GLYPH = "W"

@Composable
fun DedGrid(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
    textStyle: TextStyle = LocalTextStyle.current,
    colors: DedColors = LocalDefaults.current.editor
) {
    val textMeasurer = rememberTextMeasurer()
    var cellSize by remember { mutableStateOf(IntSize(16, 24)) }
    var windowSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(GLYPH, textStyle) {
        cellSize = textMeasurer.measure(GLYPH, textStyle).size
    }

    LaunchedEffect(windowSize, cellSize) {
        state.windowSizePx = windowSize
        state.windowSizeCells = windowSize.div(cellSize)
    }

    val scope = remember {
        object : DedScope {
            override val cellSize: IntSize
                get() = cellSize

            override val colors
                get() = colors

            override val lineNumberXOffset
                get() = state.lineCount.numDigits() + 1
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { windowSize = it },
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides textStyle,
        ) {
            scope.AllVisibleGlyphs(state.length, state::getCharAt, state.cursorPos)
        }
    }
}

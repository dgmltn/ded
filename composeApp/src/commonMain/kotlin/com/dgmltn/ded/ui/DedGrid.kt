package com.dgmltn.ded.ui

import DedColors
import LocalDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

private const val GLYPH = "W"

@Composable
fun DedGrid(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
    textStyle: TextStyle = LocalTextStyle.current,
    colors: DedColors = LocalDefaults.current.editor
) {
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(GLYPH, textStyle) {
        state.cellSizePx = textMeasurer.measure(GLYPH, textStyle).size
    }

    val scope = remember {
        object : DedScope {
            override val cellSize
                get() = state.cellSizePx

            override val colors
                get() = colors

            override val cellOffset: IntOffset
                get() = state.cellOffset
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { state.windowSizePx = it },
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides textStyle,
        ) {
            scope.AllVisibleGlyphs(state.length, state::getCharAt, state.cursorPos, state.selection)
        }
    }
}

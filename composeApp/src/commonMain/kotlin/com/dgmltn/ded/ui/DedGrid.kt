package com.dgmltn.ded.ui

import DedColors
import LocalDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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
import co.touchlab.kermit.Logger

private const val GLYPH = "W"

@Composable
fun DedGrid(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
    textStyle: TextStyle = LocalTextStyle.current,
    colors: DedColors = LocalDefaults.current.editor
) {
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(GLYPH, textStyle, state.windowSizePx) {
        state.cellSizePx = textMeasurer.measure(GLYPH, textStyle).size
    }

    LaunchedEffect(state.windowSizePx, state.cellSizePx, state.rowCount) {
        state.maxWindowYScrollPx = (state.rowCount * state.cellSizePx.height - state.windowSizePx.height).coerceAtLeast(0)
        if (state.windowYScrollPx > state.maxWindowYScrollPx) {
            state.windowYScrollPx = state.maxWindowYScrollPx.toFloat()
        }
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
            .onSizeChanged { state.windowSizePx = it }
            .offset { IntOffset(0, -state.windowYScrollPx.toInt()) }, // TODO: don't just offset the entire window
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides textStyle,
        ) {
            scope.AllVisibleGlyphs(state.length, state::getCharAt, state.cursorPos, state.selection)
        }
    }
}

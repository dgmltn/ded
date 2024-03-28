package com.dgmltn.ded.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalTextStyle
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
import androidx.compose.ui.unit.sp
import dedTypography

@Composable
fun Ded(
    modifier: Modifier = Modifier,
) {
    val dedState = rememberDedState()

    Box(modifier = modifier) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(fontFamily = dedTypography(), fontSize = 18.sp)
        ) {
            CellGlyph(2, 2, 'l')
            CellGlyph(5, 5, 'l')
            CellGlyphs(6, 5, "Hello, world!")
            CellGlyphs(7, 5, "This is a test")
            Cursor(5, 5)
        }
    }
}

private const val GLYPH = "W"

@Composable
fun DedGrid(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
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
        modifier = modifier
            .onSizeChanged { state.windowSizePx = it },
        content = {
            CompositionLocalProvider(LocalTextStyle provides textStyle) {
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

package com.dgmltn.ded.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.numDigits

@Composable
fun Ded(
    modifier: Modifier = Modifier,
) {
    val dedState = rememberDedState(editor = StringBuilderEditor().apply {
        insert("Hello, world\nthis is a test\nLine #3")
        move(5)
    })

    val lineNumberXOffset = dedState.editor.lineCount.numDigits() + 1

    Box(modifier = modifier.padding(5.dp)) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code.copy(fontSize = 18.sp)
        ) {
            var row = 0
            var col = 0
            LineNumber(row)
            (0 until dedState.editor.length).forEach { i ->
                val c = dedState.editor.getCharAt(i)
                if (c != '\n') CellGlyph(row, col + lineNumberXOffset, c)
                if (i == dedState.editor.cursor) {
                    Cursor(row, col + lineNumberXOffset)
                }
                col++
                if (c == '\n') {
                    row++
                    col = 0
                    LineNumber(row)
                }
            }

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

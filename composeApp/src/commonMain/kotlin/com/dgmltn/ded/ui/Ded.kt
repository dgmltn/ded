package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.div
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.editor.language.JavascriptLanguageConfig
import com.dgmltn.ded.editor.toRowCol
import com.dgmltn.ded.toInt

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor
) {
    val focusRequester = remember { FocusRequester() }

    val dedState = rememberDedState(
        editor = StringBuilderEditor(),
        languageConfig = JavascriptLanguageConfig()
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        dedState.run {
            insert("Hello, world\nthis is a test\nLine #3\n\n")
            insert("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
            moveTo(5)
        }
    }

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
            .focusRequester(focusRequester)
            .focusable()
            .dedKeyEvent(dedState)
            .detectSelectGestures(dedState)
            .detectScrollGestures(dedState)

    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code,
            colors = colors
        )

        Text(
            modifier = Modifier.align(Alignment.BottomEnd),
            text = dedState.cursorPos.toString(),
        )
    }
}

@Composable
fun Modifier.dedKeyEvent(dedState: DedState) = then(
    onKeyEvent {
        Logger.e("DOUG: $it")
        (it.type == KeyEventType.KeyDown)
                && keys[ModifiedKey(it)]?.invoke(dedState) ?: false
    }
)

@Composable
fun Modifier.detectScrollGestures(
    dedState: DedState,
) = this
    .scrollable(
        orientation = Orientation.Vertical,
        // Scrollable state: describes how to consume
        // scrolling delta and update offset
        state = rememberScrollableState { delta ->
            val min = 0f
            val max = dedState.maxWindowYScrollPx.toFloat()
            val current = dedState.windowYScrollPx
            val minDelta = min - current
            val maxDelta = max - current
            val clipped = (-delta).coerceIn(minDelta, maxDelta)
            dedState.windowYScrollPx += clipped
            -clipped
        }
    )

@Composable
fun Modifier.detectSelectGestures(
    dedState: DedState,
) = this
    .pointerInput(Unit) {
        detectTapGestures { offset ->
            val col = (offset.x / dedState.cellSizePx.width).toInt() - dedState.cellOffset.x
            val row = ((offset.y / dedState.cellSizePx.height).toInt() - dedState.cellOffset.y)
                .coerceIn(0, dedState.rowCount - 1)
            dedState.moveTo(RowCol(row, col))
        }
    }
    .pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                val cellOffset = offset.div(dedState.cellSizePx).toInt() - dedState.cellOffset
                dedState.moveTo(cellOffset.toRowCol())
            },
            onDrag = { change, _ ->
                val cellOffset =
                    change.position.div(dedState.cellSizePx).toInt() - dedState.cellOffset
                dedState.withSelection { dedState.moveTo(cellOffset.toRowCol()) }
                change.consume()
            },
        )
    }
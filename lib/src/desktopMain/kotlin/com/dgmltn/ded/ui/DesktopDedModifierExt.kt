package com.dgmltn.ded.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import com.dgmltn.ded.editor.RowCol

@Composable
actual fun Modifier.dedGestureModifier(
    dedState: DedState,
    focusRequester: FocusRequester
) = this
    .detectTapGestures(dedState, focusRequester)
    .detectSelectGestures(dedState)
    .detectScrollGestures(dedState)


@Composable
private fun Modifier.detectScrollGestures(
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
private fun Modifier.detectTapGestures(
    dedState: DedState,
    focusRequester: FocusRequester
): Modifier {
    var lastClick: Pair<Long, RowCol>? by remember { mutableStateOf(null) }
    return this
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    focusRequester.requestFocus()
                    val now = System.currentTimeMillis()
                    val cellOffset = dedState.getCellAt(offset)
                    dedState.moveTo(cellOffset)
                    lastClick?.run {
                        if (now - first < viewConfiguration.doubleTapTimeoutMillis && cellOffset == second) {
                            // double-click on the same cell
                            dedState.selectTokenAtCursor()
                        }
                    }
                    lastClick = now to cellOffset
                },
            )
        }
}

@Composable
private fun Modifier.detectSelectGestures(
    dedState: DedState,
) = this
    .pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                val cellOffset = dedState.getCellAt(offset)
                dedState.moveTo(cellOffset)
            },
            onDrag = { change, _ ->
                val cellOffset = dedState.getCellAt(change.position)
                dedState.withSelection { dedState.moveTo(cellOffset) }
                change.consume()
            },
        )
    }
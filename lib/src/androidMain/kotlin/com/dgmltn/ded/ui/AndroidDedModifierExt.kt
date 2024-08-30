package com.dgmltn.ded.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import co.touchlab.kermit.Logger

@Composable
actual fun Modifier.dedGestureModifier(
    dedState: DedState,
    focusRequester: FocusRequester
) = this
    .detectTapGestures(dedState, focusRequester)
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
            Logger.e { "delta: $delta" }
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
    val haptic = LocalHapticFeedback.current
    return this.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { offset ->
                focusRequester.requestFocus()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val cellOffset = dedState.getCellAt(offset)
                dedState.moveTo(cellOffset)
                dedState.selectTokenAtCursor()
            },
            onTap = { offset ->
                focusRequester.requestFocus()
                dedState.inputSession?.showSoftwareKeyboard()
                val cellOffset = dedState.getCellAt(offset)
                dedState.moveTo(cellOffset)
            }
        )
    }
}

@Composable
private fun Modifier.detectDragGestures(
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
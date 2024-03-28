package com.dgmltn.ded.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize


@Composable
fun rememberDedState(
    initialCursorPos: IntOffset = IntOffset.Zero
): DedState {
    return rememberSaveable(saver = DedState.Saver) {
        DedState(initialCursorPos)
    }
}

class DedState(
    initialCursorPos: IntOffset = IntOffset.Zero
) {
    var cursorPos by mutableStateOf(initialCursorPos)
    var windowSizePx by mutableStateOf(IntSize.Zero)
    var windowOffsetPx by mutableStateOf(IntOffset.Zero)

    companion object {
        /**
         * The default [Saver] implementation for [DedState].
         */
        val Saver: Saver<DedState, *> = Saver(
            save = {
                   //TODO
            },
            restore = {
                DedState()
            }
        )
    }
}
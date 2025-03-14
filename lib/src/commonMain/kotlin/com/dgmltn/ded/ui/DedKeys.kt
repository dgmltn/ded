package com.dgmltn.ded.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import co.touchlab.kermit.Logger
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.toIntRange

@Composable
fun Modifier.dedKeyEvent(dedState: DedState) = then(
    onKeyEvent {
        if (it.type != KeyEventType.KeyDown) return@onKeyEvent false
        val handler = keys[ModifiedKey(it)]
        if (handler != null) {
            handler(dedState)
            return@onKeyEvent true
        }

        if (it.utf16CodePoint == 0xffff || it.utf16CodePoint == 0x0) return@onKeyEvent false

        Logger.e { "DOUG: codePoint: ${it.utf16CodePoint}" }

        dedState.insert(it.consumeCodePoint())
        true
    }
)

data class ModifiedKey(
    val key: Key,
    val shift: Boolean = false,
    val meta: Boolean = false,
    val ctrl: Boolean = false,
    val alt: Boolean = false
) {
    constructor(keyEvent: KeyEvent) : this(
        key = keyEvent.key,
        shift = keyEvent.isShiftPressed,
        meta = keyEvent.isMetaPressed,
        ctrl = keyEvent.isCtrlPressed,
        alt = keyEvent.isAltPressed
    )
}

private fun KeyEvent.consumeCodePoint(): String {
    return StringBuilder().appendCodePoint(utf16CodePoint).toString()
}

fun Key.modified(
    shift: Boolean = false,
    meta: Boolean = false,
    ctrl: Boolean = false,
    alt: Boolean = false
) =
    ModifiedKey(this, shift, meta, ctrl, alt)

val keys: Map<ModifiedKey, DedState.() -> Boolean> = mapOf(
    // Movement
    Key.DirectionLeft.modified() to { moveBy(-1) },
    Key.DirectionRight.modified() to { moveBy(1) },
    Key.DirectionDown.modified() to { moveBy(RowCol(1, 0)) },
    Key.DirectionUp.modified() to { moveBy(RowCol(-1, 0)) },
    Key.DirectionLeft.modified(shift = true) to { withSelection { moveBy(-1) } },
    Key.DirectionRight.modified(shift = true) to { withSelection { moveBy(1) } },
    Key.DirectionDown.modified(shift = true) to { withSelection { moveBy(RowCol(1, 0)) } },
    Key.DirectionUp.modified(shift = true) to { withSelection { moveBy(RowCol(-1, 0)) } },
    Key.DirectionRight.modified(meta = true) to { moveToEndOfLine() },
    Key.DirectionLeft.modified(meta = true) to { moveToBeginningOfLine() },
    Key.DirectionRight.modified(meta = true, shift = true) to { withSelection { moveToEndOfLine() } },
    Key.DirectionLeft.modified(meta = true, shift = true) to { withSelection { moveToBeginningOfLine() } },

    // Spacing/newlines
    Key.Tab.modified() to { tab() },
    Key.Backspace.modified() to { selection?.toIntRange()?.let { delete(it) } ?: backspace() },
    Key.Delete.modified() to { selection?.toIntRange()?.let { delete(it) } ?: delete() },

    // Undo/redo
    Key.Z.modified(meta = true) to { undo() },
    Key.Z.modified(meta = true, shift = true) to { redo() },

    // Clipboard
    Key.Cut.modified() to { cut() },
    Key.X.modified(meta = true) to { cut() },
    Key.Copy.modified() to { copy() },
    Key.C.modified(meta = true) to { copy() },
    Key.Paste.modified() to { paste() },
    Key.V.modified(meta = true) to { paste() },

)

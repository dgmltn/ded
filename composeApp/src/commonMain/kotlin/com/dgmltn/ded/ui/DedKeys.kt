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

// Copy from https://github.com/JetBrains/kotlin/blob/7cd306950aad852e006715067435a4bbd9cd40d2/kotlin-native/runtime/src/main/kotlin/generated/_StringUppercase.kt#L26
private const val MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000
private fun StringBuilder.appendCodePoint(codePoint: Int): StringBuilder {
    if (codePoint < MIN_SUPPLEMENTARY_CODE_POINT) {
        append(codePoint.toChar())
    } else {
        append(Char.MIN_HIGH_SURROGATE + ((codePoint - 0x10000) shr 10))
        append(Char.MIN_LOW_SURROGATE + (codePoint and 0x3ff))
    }
    return this
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

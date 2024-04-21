package com.dgmltn.ded.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.dgmltn.ded.editor.Editor
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.editor.StringBuilderEditor


@Composable
fun rememberDedState(
    editor: Editor = StringBuilderEditor(),
): DedState {
    return rememberSaveable(saver = DedState.Saver) {
        DedState(editor)
    }
}

class DedState(
    private val editor: Editor = StringBuilderEditor(),
) {
    var cursorPos by mutableStateOf(editor.cursor)
    var length by mutableStateOf(editor.length)
    var lineCount by mutableStateOf(editor.lineCount)
    var windowSizePx by mutableStateOf(IntSize.Zero)
    var windowOffsetPx by mutableStateOf(IntOffset.Zero)

    fun moveNextRow() {
        editor.moveBy(RowCol(1, 0))
        cursorPos = editor.cursor
    }

    fun movePrevRow() {
        editor.moveBy(RowCol(-1, 0))
        cursorPos = editor.cursor
    }

    fun moveFwd() {
        editor.moveBy(1)
        cursorPos = editor.cursor
    }

    fun moveBack() {
        editor.moveBy(-1)
        cursorPos = editor.cursor
    }

    fun getCharAt(position: Int) = editor.getCharAt(position)

    fun getRowColOfCursor() = editor.getRowColOfCursor()

    fun insert(value: String) {
        editor.insert(value)
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
    }

    fun delete(count: Int) {
        editor.delete(count)
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
    }

    fun backspace() {
        if (cursorPos == 0) return
        editor.moveBy(-1)
        editor.delete(1)
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
    }

    fun undo() {
        editor.undo()
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
    }

    fun redo() {
        editor.redo()
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
    }

    companion object {
        /**
         * The default [Saver] implementation for [DedState].
         */
        val Saver: Saver<DedState, *> = Saver(
            save = {
                   null
            },
            restore = {
                DedState()
            }
        )
    }
}
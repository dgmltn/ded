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
import com.dgmltn.ded.editor.language.JavascriptLanguageConfig
import com.dgmltn.ded.editor.language.LanguageConfig
import com.dgmltn.ded.numDigits

@Composable
fun rememberDedState(
    editor: Editor = StringBuilderEditor(),
    languageConfig: LanguageConfig = JavascriptLanguageConfig()
): DedState {
    return rememberSaveable(saver = DedState.Saver) {
        DedState(editor, languageConfig)
    }
}

class DedState(
    private val editor: Editor = StringBuilderEditor(),
    val languageConfig: LanguageConfig = JavascriptLanguageConfig()
) {
    var cursorPos by mutableStateOf(editor.cursor)
    var length by mutableStateOf(editor.length)
    var lineCount by mutableStateOf(editor.lineCount)
    var cellOffset by mutableStateOf(IntOffset.Zero)

    var windowSizePx by mutableStateOf(IntSize.Zero)
    var windowOffsetPx by mutableStateOf(IntOffset.Zero)
    var cellSizePx by mutableStateOf(IntSize.Zero)

    fun moveNextRow(): Boolean {
        if (cursorPos == length) return false
        editor.moveBy(RowCol(1, 0))
        syncWithEditor()
        return true
    }

    fun movePrevRow(): Boolean {
        if (cursorPos == 0) return false
        editor.moveBy(RowCol(-1, 0))
        syncWithEditor()
        return true
    }

    fun moveFwd(): Boolean {
        if (cursorPos == length) return false
        editor.moveBy(1)
        syncWithEditor()
        return true
    }

    fun moveBack(): Boolean {
        if (cursorPos == 0) return false
        editor.moveBy(-1)
        syncWithEditor()
        return true
    }

    fun moveTo(position: Int): Int {
        editor.moveTo(position)
        syncWithEditor()
        return cursorPos
    }

    fun moveTo(rowCol: RowCol): Int {
        editor.moveTo(rowCol)
        syncWithEditor()
        return cursorPos
    }

    fun getCharAt(position: Int) = editor.getCharAt(position)

    fun getRowColOfCursor() = editor.getRowColOfCursor()

    fun insert(value: String) = editor.insert(value).also { syncWithEditor() }

    fun delete(count: Int) = editor.delete(count).also { syncWithEditor() }

    fun backspace(): Boolean {
        if (cursorPos == 0) return false
        editor.moveBy(-1)
        editor.delete(1)
        syncWithEditor()
        return true
    }

    fun undo() = editor.undo().also { syncWithEditor() }

    fun redo() = editor.redo().also { syncWithEditor() }

    private fun syncWithEditor() {
        cursorPos = editor.cursor
        length = editor.length
        lineCount = editor.lineCount
        cellOffset = cellOffset.copy(x = editor.lineCount.numDigits() + 1)
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
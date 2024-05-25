package com.dgmltn.ded.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
    var selection by mutableStateOf(editor.selection)
    var length by mutableStateOf(editor.length)
    var rowCount by mutableStateOf(editor.rowCount)
    var cellOffset by mutableStateOf(IntOffset.Zero)

    var windowSizePx by mutableStateOf(IntSize.Zero)
    var maxWindowXScrollPx by mutableIntStateOf(0)
    var windowXScrollPx by mutableFloatStateOf(0f)
    var maxWindowYScrollPx by mutableIntStateOf(0)
    var windowYScrollPx by mutableFloatStateOf(0f)
    var cellSizePx by mutableStateOf(IntSize.Zero)

    fun moveBy(rowCol: RowCol) = (editor.moveBy(rowCol) > -1).also { syncWithEditor() }

    fun moveBy(delta: Int) = (editor.moveBy(delta) == delta).also { syncWithEditor() }

    fun moveTo(position: Int) = (editor.moveTo(position) == position).also { syncWithEditor() }

    fun moveTo(rowCol: RowCol) = (editor.moveTo(rowCol) > -1).also { syncWithEditor() }

    fun moveToBeginningOfLine() = moveTo(getBeginningOfLinePos())

    fun moveToEndOfLine() = moveTo(getEndOfLinePos())

    fun getCharAt(position: Int) = editor.getCharAt(position)

    fun getRowColOfCursor() = editor.getRowColOfCursor()

    fun getRangeOfRow(row: Int) = editor.getRangeOfRow(row)

    fun insert(value: String) = editor.insert(value).also { syncWithEditor() }

    fun delete(count: Int) = editor.delete(count).also { syncWithEditor() }

    fun backspace() = (editor.backspace(1) == 1).also { syncWithEditor() }

    fun undo() = editor.undo().also { syncWithEditor() }

    fun redo() = editor.redo().also { syncWithEditor() }

    private fun syncWithEditor() {
        cursorPos = editor.cursor
        selection = editor.selection
        length = editor.length
        rowCount = editor.rowCount
        cellOffset = cellOffset.copy(x = editor.rowCount.numDigits() + 1)

        // In case rowCount changed
        maxWindowYScrollPx = (rowCount * cellSizePx.height - windowSizePx.height).coerceAtLeast(0)
        if (windowYScrollPx > maxWindowYScrollPx) {
            windowYScrollPx = maxWindowYScrollPx.toFloat()
        }
        // Make sure cursor is visible
        val cursorRow = editor.getRowColOfCursor().row
        val minVisibleRow = (cursorRow - 1).coerceAtLeast(0)
        val maxVisibleRow = (cursorRow + 2)
        if (minVisibleRow * cellSizePx.height < windowYScrollPx) {
            windowYScrollPx = (minVisibleRow * cellSizePx.height).toFloat()
        }
        else if (maxVisibleRow * cellSizePx.height > windowYScrollPx + windowSizePx.height) {
            windowYScrollPx = (maxVisibleRow * cellSizePx.height - windowSizePx.height).toFloat()
        }


    }

    /**
     * Returns the position of the first non-whitespace character on the
     * current line.
     *
     * Special handling for moving to the beginning of the line: If the
     * cursor is already at the first non-whitespace character, it will
     * return the beginning of the line instead.
     */
    private fun getBeginningOfLinePos(): Int {
        val cursor = getRowColOfCursor()
        val rangeOfRow = getRangeOfRow(cursor.row)
        val indent = rangeOfRow.firstOrNull { !getCharAt(it).isWhitespace() }?.minus(rangeOfRow.first) ?: 0
        val pos = rangeOfRow.first + if (cursor.col == indent) 0 else indent
        return pos
    }

    /**
     * Returns the position of the end of the current line.
     *
     * Special handling for the last line of the file: If the cursor is
     * on the last line of the file, will return [length] instead of to the last
     * character of the line.
     */
    private fun getEndOfLinePos(): Int {
        val cursor = getRowColOfCursor()
        val pos = if (cursor.row == rowCount - 1) length else getRangeOfRow(cursor.row).last
        return pos
    }

    fun withSelection(action: () -> Unit): Boolean {
        val from = editor.selection?.first ?: editor.cursor
        action()
        editor.select(when {
            from <= editor.cursor -> from..editor.cursor
            else -> from downTo editor.cursor
        })
        syncWithEditor()
        return true
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
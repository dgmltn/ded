package com.dgmltn.ded.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextInputSession
import androidx.compose.ui.unit.IntSize
import com.dgmltn.ded.editor.Editor
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.editor.language.JavascriptLanguageConfig
import com.dgmltn.ded.editor.language.LanguageConfig
import com.dgmltn.ded.theme.DedColors
import com.dgmltn.ded.theme.DedDefaults
import com.dgmltn.ded.toIntRange

@Composable
fun rememberDedState(
    initialValue: String = "",
    editor: Editor = StringBuilderEditor(initialValue),
    colors: DedColors = DedDefaults.colors,
    languageConfig: LanguageConfig = JavascriptLanguageConfig(),
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
): DedState {
    return remember {
        DedState(initialValue, clipboardManager, colors, editor, languageConfig)
    }
}

class DedState(
    initialValue: String? = null,
    private val clipboardManager: ClipboardManager,
    private val colors: DedColors = DedDefaults.colors,
    private val editor: Editor = StringBuilderEditor(initialValue),
    private val languageConfig: LanguageConfig = JavascriptLanguageConfig(),
) {
    // The full text of the editor. This should be optimized but for now it's needed
    // to build highlights.
    var value by mutableStateOf(editor.value)

    // The current cursor position of the editor
    var cursor by mutableStateOf(editor.cursor)

    // The current selection range of the editor
    var selection by mutableStateOf(editor.selection)

    // The length of the editor's text
    var length by mutableStateOf(editor.length)

    // The number of rows in the editor
    var rowCount by mutableStateOf(editor.rowCount)

    // The number of glyphs that the line number will take up
    var lineNumberLength by mutableIntStateOf(0)

    // Used to map clicks and pixel positions to cell positions
    var windowSizePx by mutableStateOf(IntSize.Zero)
    var cellSizePx by mutableStateOf(IntSize.Zero)
    var maxWindowYScrollPx by mutableIntStateOf(0)
    var windowYScrollPx by mutableFloatStateOf(0f)

    // Calculated by the highlighter library
//    private var highlights by mutableStateOf(emptyList<CodeHighlight>())

    // Related to the software keyboard
    var inputSession: TextInputSession? = null

    init {
        syncWithEditor()
    }

    fun moveBy(rowCol: RowCol) = (editor.moveBy(rowCol) > -1).also { syncWithEditor() }

    fun moveBy(delta: Int) = (editor.moveBy(delta) == delta).also { syncWithEditor() }

    fun moveTo(position: Int) = (editor.moveTo(position) == position).also { syncWithEditor() }

    fun moveTo(rowCol: RowCol) = (editor.moveTo(rowCol) > -1).also { syncWithEditor() }

    fun getPositionOf(rowCol: RowCol) = editor.getPositionOf(rowCol)

    fun selectTokenAt(rowCol: RowCol) {
        val range = editor.getRangeOfToken(editor.getPositionOf(rowCol))
        editor.moveTo(range.last)
        editor.select(range)
        syncWithEditor()
    }

    fun moveToBeginningOfLine() = moveTo(getBeginningOfLinePos())

    fun moveToEndOfLine() = moveTo(getEndOfLinePos())

    fun getCharAt(position: Int) = editor.getCharAt(position)

    fun insert(value: String) = editor.insert(value).also { syncWithEditor() }

    fun tab(): Boolean {
        val tabSize = languageConfig.tabSize
        val col = editor.getRowColOfCursor().col
        val numOfSpaces = tabSize - (col % tabSize)
        return insert(" ".repeat(numOfSpaces))
    }

    /**
     * Returns true if the delete was successful.
     */
    fun delete() = (editor.delete(1) == 1).also { syncWithEditor() }

    /**
     * Returns true if the selection was deleted. Cursor will be at the beginning of the selection.
     */
    fun delete(range: IntRange) =
        range.run {
            editor.moveTo(first)
            editor.delete(count()) == count()
        }.also { syncWithEditor() }

    /**
     * Returns true if the backspace was successful.
     */
    fun backspace() = (editor.backspace(1) == 1).also { syncWithEditor() }

    fun undo() = editor.undo().also { syncWithEditor() }

    fun redo() = editor.redo().also { syncWithEditor() }

    fun cut(): Boolean {
        copy()
        return insert("")
    }

    fun copy(): Boolean {
        val localSelection = selection
            ?.toIntRange()
            ?: editor.getRangeOfRow(editor.getRowColOfCursor().row)
                .also {
                    editor.select(it)
                    syncWithEditor()
                }

        val text = editor.getSubstring(localSelection)
        clipboardManager.setText(AnnotatedString(text))
        return true
    }

    fun paste() = insert(clipboardManager.getText().toString())

    /**
     * Returns the Cell position of the given pixel offset [offset]. This takes into account
     * the current window scroll position, cell size, and indentation due to line numbers.
     */
    fun getCellAt(offset: Offset) =
        RowCol(
            row = ((offset.y + windowYScrollPx) / cellSizePx.height).toInt(),
            col = (offset.x / cellSizePx.width - lineNumberLength).toInt()
        )

    private val colorCache = mutableMapOf<Int, Color>()
    fun getColorOf(position: Int): Color? =
        null

    private fun syncWithEditor() {
        value = editor.value
        cursor = editor.cursor
        selection = editor.selection
        length = editor.length
        rowCount = editor.rowCount
        lineNumberLength = "${editor.getLastRow() + 1} ".length

        // In case rowCount changed
        maxWindowYScrollPx = (rowCount * cellSizePx.height - windowSizePx.height).coerceAtLeast(0)
        if (windowYScrollPx > maxWindowYScrollPx) {
            windowYScrollPx = maxWindowYScrollPx.toFloat()
        }

        // Make sure cursor is visible
        val cursorRow = editor.getRowColOfCursor().row
        val minVisibleRow = (cursorRow - 1).coerceAtLeast(0)
        val maxVisibleRow = (cursorRow + 1)
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
        val cursor = editor.getRowColOfCursor()
        val rangeOfRow = editor.getRangeOfRow(cursor.row)
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
        val cursor = editor.getRowColOfCursor()
        val pos = if (cursor.row == rowCount - 1) length else editor.getRangeOfRow(cursor.row).last
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
}
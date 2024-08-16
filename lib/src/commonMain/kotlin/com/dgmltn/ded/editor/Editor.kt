package com.dgmltn.ded.editor


interface Editor {
    // Editing
    /**
     * Cursor can be between 0..length. Special cases have to
     * be handled when cursor == length.
     */
    var cursor: Int

    /**
     * Selection is a range of characters that are selected.
     * If selection is null, nothing is selected.
     */
    var selection: IntProgression?

    // Reading
    val value: String
    val rowCount: Int
    val length: Int

    /**
     * Moves the cursor to [position]. Can be any number. If it's outside
     * the range 0..length, Editor will coerce in range.
     */
    fun moveTo(position: Int): Int {
        val l = length
        cursor = position.coerceIn(0, l)
        selection = null
        return cursor
    }

    fun moveTo(rowCol: RowCol): Int {
        val pos = getPositionOf(rowCol)
        return moveTo(pos)
    }

    fun moveBy(delta: Int): Int {
        return moveTo(cursor + delta)
    }

    fun moveBy(rowColDelta: RowCol): Int {
        val rowCol = getRowColOfCursor()
        val nextRow = rowCol.row + rowColDelta.row
        return if (nextRow < 0) {
            moveTo(0)
        }
        else if (nextRow >= rowCount) {
            moveTo(length)
        }
        else {
            val row = nextRow.coerceIn(0, rowCount - 1)
            val rowRange = getRangeOfRow(row)
            val rowCount = rowRange.count()
            val position = rowRange.first + (rowCol.col + rowColDelta.col).coerceIn(0, rowCount - 1)
            moveTo(position)
        }
    }

    fun select(range: IntProgression) {
        selection = range
    }

    /**
     * Returns true if the value was inserted. If [selection] is nonnull,
     * the selection will be replaced.
     */
    fun insert(value: String): Boolean

    /**
     * Returns the number of characters that were actually deleted
     */
    fun delete(count: Int): Int

    /**
     * Returns the number of characters that were actually backspaced
     */
    fun backspace(count: Int): Int

    /**
     * Returns the number of characters that were actually replaced.
     * Will replace up to [count] characters with the new value.
     */
    fun replace(count: Int, value: String): Int

    fun canUndo(): Boolean

    fun undo(): Boolean

    fun canRedo(): Boolean

    fun redo(): Boolean

    fun getCharAt(position: Int): Char

    /**
     * Returns the range of characters that make up the token/word at [position]. If the
     * character at [position] is whitespace, the range returned will be the block of
     * whitespace.
     */
    fun getRangeOfToken(
        position: Int,
        isIdentifierChar: (Char) -> Boolean = { it.isLetterOrDigit() || it == '_' }
    ): IntRange {
        if (length == 0 || position < 0 || position >= length) {
            return IntRange.EMPTY
        }

        val isInsideToken = isIdentifierChar(getCharAt(position))

        // Find the start of the token
        var start = position
        while (start > 0 && isInsideToken == isIdentifierChar(getCharAt(start - 1))) {
            start--
        }

        // Find the end of the token
        var end = position
        while (end < (length - 1) && isInsideToken == isIdentifierChar(getCharAt(end + 1))) {
            end++
        }

        return IntRange(start, end)
    }

    /**
     * Returns a new String that contains characters in this Editor
     * at startPosition (inclusive) and up to the endPosition (exclusive).
     *
     * If endPosition is greater than length, it will be coerced to length.
     */
    fun getSubstring(startPosition: Int, endPosition: Int): String

    /**
     * Returns a new String that contains characters in this Editor
     * at the range of [range].
     */
    fun getSubstring(range: IntRange) =
        getSubstring(range.first, range.last + 1)

    fun getRangeOfRow(row: Int): IntRange

    fun getPositionOf(rowCol: RowCol): Int {
        val lastRow = getLastRow()
        val coercedRow = rowCol.row.coerceIn(0, lastRow)
        val rowRange = getRangeOfRow(coercedRow)
        return when(coercedRow) {
            lastRow -> rowRange.first + rowCol.col.coerceIn(0 until rowRange.count() + 1)
            else -> rowRange.first + rowCol.col.coerceIn(0 until rowRange.count())
        }
    }

    fun getLastRow() = (rowCount - 1).coerceAtLeast(0)

    fun getRowOf(position: Int): Int

    fun getRowColOf(position: Int): RowCol {
        val row = getRowOf(position)
        val rangeOfRow = getRangeOfRow(row)
        val col = position - rangeOfRow.first
        return RowCol(row, col)
    }

    fun getRowColOfCursor() = getRowColOf(cursor)
}
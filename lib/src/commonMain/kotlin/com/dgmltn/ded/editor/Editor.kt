package com.dgmltn.ded.editor


interface Editor {
    // Editing
    /**
     * Cursor can be between 0..length. Special cases have to
     * be handled when cursor == length.
     */
    var cursor: Int

    /**
     * Cache the RowCol of the cursor.
     */
    var cursorRowCol: RowCol?

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
        cursor = position.coerceIn(0, length)
        cursorRowCol = null
        selection = null
        return cursor
    }

    fun moveTo(rowCol: RowCol): Int {
        val pos = getPositionOf(rowCol)
        return moveTo(pos)
    }

    fun moveBy(delta: Int): Int {
        if (delta > 0) {
            val coercedDelta = delta.coerceAtMost(length - cursor)
            var (row, col) = getRowColOfCursor()
            var previous = getCharAt(cursor)
            (1 .. coercedDelta).forEach {
                if (previous == '\n') {
                    row++
                    col = 0
                }
                else {
                    col++
                }
                previous = getCharAt(cursor + it)
            }
            cursorRowCol = RowCol(row, col)
            cursor += coercedDelta
            selection = null
        }
        else if (delta < 0) {
            val coercedDelta = (-delta).coerceAtMost(cursor)
            var (row, col) = getRowColOfCursor()
            (1 ..  coercedDelta).forEach {
                val previous = getCharAt(cursor - it)
                if (previous == '\n') {
                    row--
                    col = getRangeOfRow(row).run { last - first }
                }
                else {
                    col--
                }
            }
            cursorRowCol = RowCol(row, col)
            cursor -= coercedDelta
            selection = null
        }
        return cursor
    }

    fun moveBy(rowColDelta: RowCol): Int {
        val (row, col) = getRowColOfCursor()
        val nextRow = row + rowColDelta.row
        if (nextRow < 0) {
            moveTo(0)
            cursor = 0
            selection = null
            cursorRowCol = RowCol(0, 0)
        }
        else if (nextRow >= rowCount) {
            cursor = length
            selection = null
            cursorRowCol = null
        }
        else {
            val rowRange = getRangeOfRow(nextRow)
            val rowCount = rowRange.run { last - first }
            val nextCol = (col + rowColDelta.col).coerceIn(0, rowCount)
            val nextCursor = rowRange.first + nextCol
            cursor = nextCursor
            selection = null
            cursorRowCol = RowCol(nextRow, nextCol)
        }
        return cursor
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

    /**
     * Returns the range of characters in a particular row, inclusive to the first
     * and last position.
     */
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
        // Special case for cursor
        if (position == cursor) cursorRowCol?.let { return it }

        val row = getRowOf(position)
        val rangeOfRow = getRangeOfRow(row)
        val col = position - rangeOfRow.first

        return RowCol(row, col)
            // Special case for cursor
            .also { if (position == cursor) cursorRowCol = it }
    }

    fun getRowColOfCursor() = getRowColOf(cursor)
}
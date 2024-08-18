package com.dgmltn.ded.editor


interface Editor {
    // Editing
    /**
     * Cursor can be between 0..length. Special cases have to
     * be handled when cursor == length.
     */
    var cursor: Int
    var cursorRow: Int?
    var cursorCol: Int?

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
        cursorRow = null
        cursorCol = null
        selection = null
        return cursor
    }

    fun moveTo(rowCol: RowCol): Int {
        val lastRow = getLastRow()
        val nextRow = rowCol.row.coerceIn(0, lastRow)
        val rowRange = getRangeOfRow(nextRow)
        val nextCol = when(nextRow) {
            lastRow -> rowCol.col.coerceIn(0 .. rowRange.count())
            else -> rowCol.col.coerceIn(0 until rowRange.count())
        }
        cursor = rowRange.first + nextCol
        cursorRow = nextRow
        cursorCol = nextCol
        selection = null
        return cursor
    }

    fun moveBy(delta: Int): Int {
        if (delta > 0) {
            val coercedDelta = delta.coerceAtMost(length - cursor)
            var row = getRowOfCursor()
            var col = getColOfCursor()
            var previous = if (cursor == length) null else getCharAt(cursor)
            (1 .. coercedDelta).forEach {
                if (previous == '\n') {
                    row++
                    col = 0
                }
                else {
                    col++
                }
                if (it != coercedDelta) previous = getCharAt(cursor + it)
            }
            cursorRow = row
            cursorCol = col
            cursor += coercedDelta
            selection = null
        }
        else if (delta < 0) {
            val coercedDelta = (-delta).coerceAtMost(cursor)
            var row = getRowOfCursor()
            var col = getColOfCursor()
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
            cursorRow = row
            cursorCol = col
            cursor -= coercedDelta
            selection = null
        }
        return cursor
    }

    fun moveBy(rowColDelta: RowCol): Int {
        val nextRow = getRowOfCursor() + rowColDelta.row
        if (nextRow < 0) {
            cursor = 0
            selection = null
            cursorRow = 0
            cursorCol = 0
        }
        else if (nextRow >= rowCount) {
            cursor = length
            selection = null
            cursorRow = null
            cursorCol = null
        }
        else {
            val rowRange = getRangeOfRow(nextRow)
            val rowCount = rowRange.run { last - first }
            val nextCol = (getColOfCursor() + rowColDelta.col).coerceIn(0, rowCount)
            val nextCursor = rowRange.first + nextCol
            cursor = nextCursor
            selection = null
            cursorRow = nextRow
            cursorCol = nextCol
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
    fun getSubstring(range: IntRange): String =
        getSubstring(range.first, range.last + 1)

    /**
     * Returns the range of characters in a particular row, inclusive to the first
     * and last position.
     */
    fun getRangeOfRow(row: Int): IntRange

    fun getLastRow(): Int = (rowCount - 1).coerceAtLeast(0)

    fun getRowOf(position: Int): Int

    private fun calculateCursorCol(): Int {
        val row = cursorRow ?: getRowOf(cursor).also { cursorRow = it }
        val rangeOfRow = getRangeOfRow(row)
        return (cursor - rangeOfRow.first).also { cursorCol = it }
    }

    fun getRowOfCursor(): Int =
        cursorRow ?: getRowOf(cursor).also { cursorRow = it }

    fun getColOfCursor(): Int =
        cursorCol ?: calculateCursorCol()
}
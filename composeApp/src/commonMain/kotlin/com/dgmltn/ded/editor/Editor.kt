package com.dgmltn.ded.editor


interface Editor {
    // Editing
    /**
     * Cursor can be between 0..length. Special cases have to be handled when cursor == length.
     */
    var cursor: Int

    /**
     * Selection is a range of characters that are selected.
     * If selection is null, nothing is selected.
     */
    var selection: IntProgression?

    /**
     * [position] can be any number. If it's outside the range 0..length, Editor will
     * coerce in range.
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
        val rowCol = getRowColOf(cursor)
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
     * Returns the number of characters that were actually replaced.
     * Will replace up to [count] characters with the new value.
     */
    fun replace(count: Int, value: String): Int

    fun canUndo(): Boolean

    fun undo(): Boolean

    fun canRedo(): Boolean

    fun redo(): Boolean

    // Reading
    val value: String
    val rowCount: Int
    val length: Int

    fun getCharAt(position: Int): Char

    fun getSubstring(startPosition: Int, endPosition: Int): String

    fun getSubstring(range: IntRange) =
        getSubstring(range.first, range.last + 1)

    fun getRangeOfAllRows(): List<IntRange>

    fun getRangeOfRow(row: Int): IntRange

    fun getPositionOf(rowCol: RowCol): Int {
        val lastRow = rowCount - 1
        val coercedRow = rowCol.row.coerceIn(0, lastRow)
        val rowRange = getRangeOfRow(coercedRow)
        return when(coercedRow) {
            lastRow -> rowRange.first + rowCol.col.coerceIn(0 until rowRange.count() + 1)
            else -> rowRange.first + rowCol.col.coerceIn(0 until rowRange.count())
        }
    }

    fun getRowColOf(position: Int): RowCol

    fun getRowColOfCursor() = getRowColOf(cursor)
}
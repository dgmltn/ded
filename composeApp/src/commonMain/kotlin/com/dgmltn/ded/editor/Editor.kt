package com.dgmltn.ded.editor

data class RowCol(val row: Int, val col: Int) {
    companion object {
        val Zero = RowCol(0, 0)
    }
}

interface Editor {
    // Editing
    /**
     * Cursor can be 0 - length. Special cases have to be handled when cursor == length.
     */
    var cursor: Int

    /**
     * [position] can be any number. If it's outside the range 0..length, Editor will
     * coerce in range.
     */
    fun moveTo(position: Int) {
        val l = length
        cursor = position.coerceIn(0, l)
    }

    fun moveTo(rowCol: RowCol) {
        moveTo(getPositionOf(rowCol))
    }

    fun moveBy(delta: Int) {
        moveTo(cursor + delta)
    }

    fun moveBy(rowColDelta: RowCol) {
        val rowCol = getRowColOf(cursor)
        val nextRow = rowCol.row + rowColDelta.row
        if (nextRow < 0) {
            moveTo(0)
        }
        else if (nextRow >= lineCount) {
            moveTo(length)
        }
        else {
            val row = nextRow.coerceIn(0, lineCount - 1)
            val rowRange = getRangeOfRow(row)
            val rowCount = rowRange.count()
            val position = rowRange.first + (rowCol.col + rowColDelta.col).coerceIn(0, rowCount - 1)
            moveTo(position)
        }
    }

    fun insert(value: String)

    /**
     * Returns the number of characters that were actually deleted
     */
    fun delete(count: Int): Int

    fun canUndo(): Boolean

    fun undo()

    fun canRedo(): Boolean

    fun redo()

    // Reading
    val value: String
    val lineCount: Int
    val length: Int

    fun getCharAt(position: Int): Char

    fun getSubstring(startPosition: Int, endPosition: Int): String

    fun getSubstring(range: IntRange) =
        getSubstring(range.first, range.last + 1)

    fun getRangeOfAllRows(): List<IntRange>

    fun getRangeOfRow(row: Int): IntRange

    fun getPositionOf(rowCol: RowCol): Int {
        val line = getRangeOfRow(rowCol.row)
        return line.first + rowCol.col.coerceIn(0 until line.count())
    }

    fun getRowColOf(position: Int): RowCol

    fun getRowColOfCursor() = getRowColOf(cursor)
}
package com.dgmltn.ded.editor

import co.touchlab.kermit.Logger

class StringBuilderEditor: Editor {
    override var cursor = 0

    override var selection: IntRange? = null

    override val value: String
        get() = builder.toString()

    override val lineCount: Int
        get() = if (length == 0) 0 else builder.count { it == '\n' } + if (builder.last() == '\n') 0 else 1

    override val length: Int
        get() = builder.length

    private val builder = StringBuilder()

    private val edits = EditsBuffer()

    override fun insert(value: String): Boolean {
        val edit = selection
            ?.let { Edit.Replace(it.first, getSubstring(it), value) }
            ?: Edit.Insert(cursor, value)
        selection = null
        edits.add(edit)
        return perform(edit)
    }

    override fun delete(count: Int): Int {
        val maxDeletableCount = length - cursor
        val adjusted = count.coerceAtMost(maxDeletableCount)
        if (adjusted <= 0) return 0

        val value = getSubstring(cursor, cursor + adjusted)
        val edit = Edit.Delete(cursor, value)
        edits.add(edit)
        perform(edit)
        return adjusted
    }

    override fun replace(count: Int, value: String): Int {
        val maxDeletableCount = length - cursor
        val adjusted = count.coerceIn(0, maxDeletableCount)

        val oldValue = getSubstring(cursor, cursor + adjusted)
        val edit = Edit.Replace(cursor, oldValue, value)
        edits.add(edit)
        perform(edit)
        return adjusted
    }

    override fun canUndo() = edits.canUndo()

    override fun undo(): Boolean {
        if (!canUndo()) return false
        val edit = edits.undo().undo()
        perform(edit)
        return true
    }

    override fun canRedo() = edits.canRedo()

    override fun redo(): Boolean {
        if (!canRedo()) return false
        val edit = edits.redo()
        perform(edit)
        return true
    }

    override fun getCharAt(position: Int) = builder[position]

    override fun getSubstring(startPosition: Int, endPosition: Int): String =
        builder.substring(startPosition, endPosition)

    override fun getRangeOfAllRows(): List<IntRange> {
        var startIndex = 0
        var endIndex = builder.indexOf("\n", startIndex)
        val lines = mutableListOf<IntRange>()
        while (endIndex != -1) {
            lines.add(startIndex .. endIndex)
            startIndex = endIndex + 1
            endIndex = builder.indexOf("\n", startIndex)
        }
        if (startIndex < builder.length && startIndex != endIndex) {
            lines.add(startIndex ..< builder.length)
        }
        return lines
    }

    override fun getRangeOfRow(row: Int): IntRange {
        // Edge case
        if (row == 0 && builder.isEmpty()) return 0..0

        var startIndex = 0
        var endIndex = builder.indexOf("\n", startIndex)
        var current = 0
        while (endIndex != -1) {
            if (current == row) return startIndex .. endIndex
            current++
            startIndex = endIndex + 1
            endIndex = builder.indexOf("\n", startIndex)
        }
        if (current == row && startIndex < builder.length && startIndex != endIndex) {
            return startIndex ..< builder.length
        }
        throw IllegalStateException("row $row is out of bounds. Should be between 0 and $current")
    }

    override fun getRowColOf(position: Int): RowCol {
        // Row = how many \n's are before position
        var row = 0

        var startIndex = 0
        var endIndex = builder.indexOf("\n", startIndex)
        while (endIndex < position && endIndex != -1) {
            row++
            startIndex = endIndex + 1
            endIndex = builder.indexOf("\n", startIndex)
        }

        val col = position - startIndex

        return RowCol(row, col)
    }

    private fun perform(edit: Edit): Boolean =
        when (edit) {
            is Edit.Insert -> {
                builder.insert(edit.position, edit.value)
                moveTo(edit.position + edit.value.length)
                true
            }
            is Edit.Delete -> {
                builder.deleteRange(edit.position, edit.position + edit.value.length)
                moveTo(edit.position)
                true
            }
            is Edit.Replace -> {
                // Can't use replaceRange because that returns a whole new StringBuilder
                builder.deleteRange(edit.position, edit.position + edit.oldValue.length)
                builder.insert(edit.position, edit.newValue)
                moveTo(edit.position + edit.newValue.length)
                true
            }
        }
}
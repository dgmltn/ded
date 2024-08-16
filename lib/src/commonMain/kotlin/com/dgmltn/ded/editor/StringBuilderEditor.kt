package com.dgmltn.ded.editor

import com.dgmltn.ded.toIntRange

class StringBuilderEditor(initialValue: String? = null): Editor {
    private val builder = if (initialValue != null) StringBuilder(initialValue) else StringBuilder()

    override var cursor = 0

    override var selection: IntProgression? = null

    override val value: String
        get() = builder.toString()

    override val length: Int
        get() = builder.length

    // Indices of newline characters
    private val newlines = LinePositionTracker(builder)

    override val rowCount: Int
        get() = newlines.rowCount

    private val edits = EditsBuffer()

    override fun insert(value: String): Boolean {
        val edit = selection
            ?.let { Edit.Replace(it.first, getSubstring(it.toIntRange()), value) }
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

    override fun backspace(count: Int): Int {
        val adjusted = count.coerceAtMost(cursor)
        if (adjusted <= 0) return 0

        val value = getSubstring(cursor - count, cursor)
        val edit = Edit.Delete(cursor - count, value)
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

    override fun getSubstring(startPosition: Int, endPosition: Int): String {
        val start = startPosition.coerceIn(0, length)
        val end = endPosition.coerceIn(0, length)
        return builder.substring(start, end)
    }

    override fun getRangeOfRow(row: Int): IntRange =
        newlines.getRangeOfRow(row)

    override fun getRowOf(position: Int): Int =
        newlines.getRowOf(position)

    private fun perform(edit: Edit): Boolean =
        when (edit) {
            is Edit.Insert -> {
                val row = newlines.getRowOf(edit.position)
                builder.insert(edit.position, edit.value)
                newlines.invalidateIndices(row)
                newlines.addNewlines(edit.value.countNewlines())
                moveTo(edit.position + edit.value.length)
                true
            }
            is Edit.Delete -> {
                val row = newlines.getRowOf(edit.position)
                builder.deleteRange(edit.position, edit.position + edit.value.length)
                newlines.invalidateIndices(row)
                newlines.addNewlines(-edit.value.countNewlines())
                moveTo(edit.position)
                true
            }
            is Edit.Replace -> {
                val row = newlines.getRowOf(edit.position)
                // Can't use replaceRange because that returns a whole new StringBuilder
                builder.deleteRange(edit.position, edit.position + edit.oldValue.length)
                builder.insert(edit.position, edit.newValue)
                newlines.invalidateIndices(row)
                newlines.addNewlines(edit.newValue.countNewlines() - edit.oldValue.countNewlines())
                moveTo(edit.position + edit.newValue.length)
                true
            }
        }

    private fun String.countNewlines() = count { it == '\n' }
}
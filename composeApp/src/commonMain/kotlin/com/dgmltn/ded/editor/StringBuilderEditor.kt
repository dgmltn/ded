package com.dgmltn.ded.editor

class StringBuilderEditor: Editor {
    override var cursor = 0

    override val value: String
        get() = builder.toString()

    override val lineCount: Int
        get() = builder.count { it == '\n' } + if (builder.last() == '\n') 0 else 1

    override val length: Int
        get() = builder.length

    private val builder = StringBuilder()

    private val edits = EditsBuffer()

    override fun move(index: Int) {
        cursor = index
    }

    override fun insert(value: String) {
        val edit = Edit.Insert(cursor, value)
        edits.add(edit)
        perform(edit)
    }

    override fun delete(count: Int) {
        val value = getSubstring(cursor, cursor + count)
        val edit = Edit.Delete(cursor, value)
        edits.add(edit)
        perform(edit)
    }

    override fun canUndo() = edits.canUndo()

    override fun undo() {
        if (!canUndo()) return
        val edit = edits.undo().undo()
        perform(edit)
    }

    override fun canRedo() = edits.canRedo()

    override fun redo() {
        if (!canRedo()) return
        val edit = edits.redo()
        perform(edit)
    }

    override fun getCharAt(index: Int) = builder[index]

    override fun getSubstring(startIndex: Int, endIndex: Int): String =
        builder.substring(startIndex, endIndex)

    override fun getSubstring(range: IntRange): String =
        getSubstring(range.first, range.last + 1)

    override fun getLines(): List<IntRange> {
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

    private fun perform(edit: Edit) {
        when (edit) {
            is Edit.Insert -> {
                builder.insert(edit.position, edit.value)
                move(edit.position + edit.value.length)
            }
            is Edit.Delete -> {
                builder.deleteRange(edit.position, edit.position + edit.value.length)
                move(edit.position)
            }
        }
    }
}
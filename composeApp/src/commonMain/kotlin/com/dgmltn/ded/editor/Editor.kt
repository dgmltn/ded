package com.dgmltn.ded.editor

import co.touchlab.kermit.Logger

data class Line(val start: Int, val end: Int, val data: String)
sealed class Edit {
    data class Insert(val position: Int, val value: String): Edit()
    data class Delete(val position: Int, val value: String): Edit()
}

interface Editor {
    // Editing
    var cursor: Int
    fun move(index: Int)
    fun insert(value: String)
    fun delete(count: Int)
    fun canUndo(): Boolean
    fun undo()
    fun canRedo(): Boolean
    fun redo()

    // Reading
    val value: String
    val lineCount: Int
    fun getSubstring(index: Int, count: Int): String
}

class EditsBuffer {
    val edits = mutableListOf<Edit>()
}

class StringBuilderEditor: Editor {
    override var cursor = 0

    override val value: String
        get() = builder.toString()

    override val lineCount: Int
        get() = builder.count { it == '\n' } + 1

    val builder = StringBuilder()

    // A list of edits performed on top of [builder].
    val edits = mutableListOf<Edit>()

    // The position within the edits list. This will
    // usually be at the end, except after an undo occurs.
    var editsIndex: Int = 0

    override fun move(index: Int) {
        cursor = index
    }

    override fun insert(value: String) {
        edits.removeAfter(editsIndex)
        val edit = Edit.Insert(cursor, value)
        edits.add(edit)
        editsIndex = edits.size
        perform(edit)
    }

    override fun delete(count: Int) {
        edits.removeAfter(editsIndex)
        val edit = Edit.Delete(cursor, getSubstring(cursor, count))
        edits.add(edit)
        editsIndex = edits.size
        perform(edit)
    }

    override fun canUndo() =
        editsIndex > 0

    override fun undo() {
        if (!canUndo()) return
        val edit = edits[--editsIndex]
        val undoEdit = when (edit) {
            is Edit.Insert -> {
                Edit.Delete(edit.position, edit.value)
            }
            is Edit.Delete -> {
                Edit.Insert(edit.position, edit.value)
            }
        }
        perform(undoEdit)
    }

    override fun canRedo() =
        editsIndex < edits.size

    override fun redo() {
        if (!canRedo()) return
        perform(edits[editsIndex++])
    }

    override fun getSubstring(index: Int, count: Int): String =
        builder.substring(index, index + count)

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

fun <T> MutableList<T>.removeAfter(index: Int) {
    while (index < size - 1) removeLast()
}
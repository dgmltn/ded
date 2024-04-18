package com.dgmltn.ded.editor


/**
 * A list of edits performed on a document.
 */
class EditsBuffer {
    val edits = mutableListOf<Edit>()

    // The position within the edits list. This will
    // usually be at the end, except after an undo occurs.
    var index = 0

    fun add(edit: Edit) {
        edits.removeAfter(index)
        edits.add(edit)
        index = edits.size
    }

    fun canUndo() = index > 0

    fun canRedo() = index < edits.size

    fun redo(): Edit {
        check(canRedo())
        return edits[index++]
    }

    fun undo(): Edit {
        check(canUndo())
        return edits[--index]
    }

    private fun <T> MutableList<T>.removeAfter(index: Int) {
        while (index < size - 1) removeLast()
    }
}
package com.dgmltn.ded.editor

sealed class Edit {
    data class Insert(val position: Int, val value: String): Edit()
    data class Delete(val position: Int, val value: String): Edit()

    // Returns an edit that will undo this edit, i.e. the opposite edit.
    fun undo() =
        when (this) {
            is Insert -> Delete(position, value)
            is Delete -> Insert(position, value)
        }
}
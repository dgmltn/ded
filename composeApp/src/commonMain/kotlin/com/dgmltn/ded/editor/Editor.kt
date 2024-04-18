package com.dgmltn.ded.editor

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
    val length: Int

    fun getCharAt(index: Int): Char
    fun getSubstring(startIndex: Int, endIndex: Int): String
    fun getSubstring(range: IntRange): String
    fun getLines(): List<IntRange>
}
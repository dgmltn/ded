package com.dgmltn.ded.editor

import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class EditorTest {

    var editor: Editor = StringBuilderEditor()

    @BeforeTest
    fun setup() {
        editor = StringBuilderEditor()
    }

    @Test
    fun test0_basic() {
        editor.cursor shouldEqual 0
        editor.canRedo() shouldEqual false
        editor.canUndo() shouldEqual false

        // Insert
        editor.moveTo(0)
        editor.insert("hello world")
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 11

        // Insert
        editor.moveTo(5)
        editor.insert(" beautiful")
        editor.value shouldEqual "hello beautiful world"
        editor.cursor shouldEqual 15

        // Undo insert
        editor.canUndo() shouldEqual true
        editor.undo()
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 5
        editor.canUndo() shouldEqual true

        // Redo insert
        editor.canRedo() shouldEqual true
        editor.redo()
        editor.value shouldEqual "hello beautiful world"
        editor.cursor shouldEqual 15
        editor.canRedo() shouldEqual false

        // Undo insert
        editor.canUndo() shouldEqual true
        editor.undo()
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 5

        // Typing
        editor.insert(" ")
        editor.insert("t")
        editor.insert("h")
        editor.insert("e")
        editor.insert("r")
        editor.insert("e")
        editor.value shouldEqual "hello there world"
        editor.cursor shouldEqual 11

        // Delete
        editor.moveTo(5)
        editor.delete(6)
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 5

        // Undo delete
        editor.canUndo() shouldEqual true
        editor.undo()
        editor.value shouldEqual "hello there world"
        editor.cursor shouldEqual 11

        // Redo delete
        editor.canRedo() shouldEqual true
        editor.redo()
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 5
        editor.canRedo() shouldEqual false
    }

    @Test
    fun test1_cursor() {
        editor.cursor shouldEqual 0
        editor.insert("hello world")
        editor.cursor shouldEqual 11
    }

    @Test
    fun test1_lineCount() {
        editor.insert("hello world")
        editor.value shouldEqual "hello world"
        editor.lineCount shouldEqual 1
        editor.insert("\n")
        editor.lineCount shouldEqual 1
        editor.insert("line #2")
        editor.lineCount shouldEqual 2
        editor.insert("\nline #3\n")
        editor.lineCount shouldEqual 3
    }

    @Test
    fun test1_lines() {
        editor.insert("hello world")
        editor.value shouldEqual "hello world"

        editor.getRangeOfAllRows()[0] shouldEqual 0 .. 10

        editor.insert("\n")
        editor.insert("line #2")
        editor.insert("\nline #3\n")

        val lines = editor.getRangeOfAllRows()
        lines[0] shouldEqual 0 .. 11
        lines[1] shouldEqual 12 .. 19
        lines[2] shouldEqual 20 .. 27
    }

    @Test
    fun test2_getLine() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.value shouldEqual "hello\nworld\nline3\nline4"
        editor.lineCount shouldEqual 4
        editor.getRangeOfRow(0) shouldEqual 0..5
        editor.getRangeOfRow(1) shouldEqual 6..11
        editor.getRangeOfRow(3) shouldEqual 18..22
    }

    @Test
    fun test3_substring() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.value shouldEqual "hello\nworld\nline3\nline4"
        editor.getSubstring(12..17) shouldEqual "line3\n"
    }

    @Test
    fun test4_getRowColOf() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.getRowColOf(15) shouldEqual RowCol(2, 3)
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
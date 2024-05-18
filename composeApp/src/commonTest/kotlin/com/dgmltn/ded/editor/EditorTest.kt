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
    fun basic_suite() {
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
    fun cursor() {
        editor.cursor shouldEqual 0
        editor.insert("hello world")
        editor.cursor shouldEqual 11
    }

    @Test
    fun moveTo() {
        editor.cursor shouldEqual 0
        editor.length shouldEqual 0
        editor.moveTo(5)
        editor.cursor shouldEqual 0
        editor.moveTo(-1)
        editor.cursor shouldEqual 0
        editor.insert("test")
        editor.moveTo(2)
        editor.cursor shouldEqual 2
        editor.moveTo(10)
        editor.cursor shouldEqual 4
        editor.moveTo(-5)
        editor.cursor shouldEqual 0
    }

    @Test
    fun lineCount() {
        editor.lineCount shouldEqual 0
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
    fun lines() {
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
    fun getLine() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.value shouldEqual "hello\nworld\nline3\nline4"
        editor.lineCount shouldEqual 4
        editor.getRangeOfRow(0) shouldEqual 0..5
        editor.getRangeOfRow(1) shouldEqual 6..11
        editor.getRangeOfRow(3) shouldEqual 18..22
    }

    @Test
    fun substring() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.value shouldEqual "hello\nworld\nline3\nline4"
        editor.getSubstring(12..17) shouldEqual "line3\n"
    }

    @Test
    fun getRowColOf() {
        editor.insert("hello\nworld\nline3\nline4")
        editor.getRowColOf(15) shouldEqual RowCol(2, 3)
    }

    @Test
    fun getRangeOfRow() {
        editor.getRangeOfRow(0) shouldEqual 0..0
        editor.insert("hello")
        editor.getRangeOfRow(0) shouldEqual 0..4
        editor.insert("\n")
        editor.getRangeOfRow(0) shouldEqual 0..5
        editor.insert("world\nline3")
        editor.getRangeOfRow(1) shouldEqual 6..11
    }

    @Test
    fun delete() {
        editor.cursor shouldEqual 0
        editor.delete(1) shouldEqual 0
        editor.insert("hello")
        editor.cursor shouldEqual 5
        editor.delete(1) shouldEqual 0
        editor.value shouldEqual "hello"
        editor.moveBy(-1)
        editor.cursor shouldEqual 4
        editor.delete(2) shouldEqual 1
        editor.value shouldEqual "hell"
        editor.moveBy(-4)
        editor.delete(1) shouldEqual 1
        editor.value shouldEqual "ell"
        editor.delete(3) shouldEqual 3
        editor.value shouldEqual ""
        editor.delete(5) shouldEqual 0
    }

    @Test
    fun undo() {
        editor.insert("hello")
        editor.undo()
        editor.value shouldEqual ""
        editor.cursor shouldEqual 0
        editor.canUndo() shouldEqual false
        editor.canRedo() shouldEqual true
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
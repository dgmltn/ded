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
        editor.run {
            cursor shouldEqual 0
            canRedo() shouldEqual false
            canUndo() shouldEqual false

            // Insert
            moveTo(0)
            insert("hello world")
            value shouldEqual "hello world"
            cursor shouldEqual 11

            // Insert
            moveTo(5)
            insert(" beautiful")
            value shouldEqual "hello beautiful world"
            cursor shouldEqual 15

            // Undo insert
            canUndo() shouldEqual true
            undo()
            value shouldEqual "hello world"
            cursor shouldEqual 5
            canUndo() shouldEqual true

            // Redo insert
            canRedo() shouldEqual true
            redo()
            value shouldEqual "hello beautiful world"
            cursor shouldEqual 15
            canRedo() shouldEqual false

            // Undo insert
            canUndo() shouldEqual true
            undo()
            value shouldEqual "hello world"
            cursor shouldEqual 5

            // Typing
            insert(" ")
            insert("t")
            insert("h")
            insert("e")
            insert("r")
            insert("e")
            value shouldEqual "hello there world"
            cursor shouldEqual 11

            // Delete
            moveTo(5)
            delete(6)
            value shouldEqual "hello world"
            cursor shouldEqual 5

            // Undo delete
            canUndo() shouldEqual true
            undo()
            value shouldEqual "hello there world"
            cursor shouldEqual 11

            // Redo delete
            canRedo() shouldEqual true
            redo()
            value shouldEqual "hello world"
            cursor shouldEqual 5
            canRedo() shouldEqual false
        }
    }

    @Test
    fun cursor() {
        editor.run {
            cursor shouldEqual 0
            insert("hello world")
            cursor shouldEqual 11
        }
    }

    @Test
    fun moveTo() {
        editor.run {
            cursor shouldEqual 0
            length shouldEqual 0
            moveTo(5)
            cursor shouldEqual 0
            moveTo(-1)
            cursor shouldEqual 0
            insert("test")
            moveTo(2)
            cursor shouldEqual 2
            moveTo(10)
            cursor shouldEqual 4
            moveTo(-5)
            cursor shouldEqual 0

        }
    }

    @Test
    fun getPositionOf() {
        editor.run {
            insert("hello\nworld\nline3\nline4")
            getPositionOf(RowCol(1, 2)) shouldEqual 8
            getPositionOf(RowCol(2, 3)) shouldEqual 15
            getPositionOf(RowCol(3, 4)) shouldEqual 22

            // Beyond the end of a row, should position to the last character in the row
            getPositionOf(RowCol(0, 100)) shouldEqual 5

            // Special case: beyond the last row, should move to the last character
            getPositionOf(RowCol(100, 0)) shouldEqual 18

            // Special case: beyond the last row, and beyond the end of the line, move to END
            getPositionOf(RowCol(100, 100)) shouldEqual 23
        }
    }

    @Test
    fun lineCount() {
        editor.run {
            rowCount shouldEqual 0
            insert("hello world")
            value shouldEqual "hello world"
            rowCount shouldEqual 1
            insert("\n")
            rowCount shouldEqual 1
            insert("line #2")
            rowCount shouldEqual 2
            insert("\nline #3\n")
            rowCount shouldEqual 3
        }
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
        editor.run {
            insert("hello\nworld\nline3\nline4")
            value shouldEqual "hello\nworld\nline3\nline4"
            rowCount shouldEqual 4
            getRangeOfRow(0) shouldEqual 0..5
            getRangeOfRow(1) shouldEqual 6..11
            getRangeOfRow(3) shouldEqual 18..22
        }
    }

    @Test
    fun substring() {
        editor.run {
            insert("hello\nworld\nline3\nline4")
            value shouldEqual "hello\nworld\nline3\nline4"
            getSubstring(12..17) shouldEqual "line3\n"
        }
    }

    @Test
    fun getRowColOf() {
        editor.run {
            insert("hello\nworld\nline3\nline4")
            getRowColOf(15) shouldEqual RowCol(2, 3)
        }
    }

    @Test
    fun getRangeOfRow() {
        editor.run {
            getRangeOfRow(0) shouldEqual 0..0
            insert("hello")
            getRangeOfRow(0) shouldEqual 0..4
            insert("\n")
            getRangeOfRow(0) shouldEqual 0..5
            insert("world\nline3")
            getRangeOfRow(1) shouldEqual 6..11
        }
    }

    @Test
    fun delete() {
        editor.run {
            cursor shouldEqual 0
            delete(1) shouldEqual 0
            insert("hello")
            cursor shouldEqual 5
            delete(1) shouldEqual 0
            value shouldEqual "hello"
            moveBy(-1)
            cursor shouldEqual 4
            delete(2) shouldEqual 1
            value shouldEqual "hell"
            moveBy(-4)
            delete(1) shouldEqual 1
            value shouldEqual "ell"
            delete(3) shouldEqual 3
            value shouldEqual ""
            delete(5) shouldEqual 0
        }
    }

    @Test
    fun replace() {
        editor.run {
            insert("hello")
            moveTo(1)
            replace(1, "a") shouldEqual 1
            value shouldEqual "hallo"
            moveTo(5)
            replace(5, " world") shouldEqual 0
            value shouldEqual "hallo world"
            moveTo(1)
            replace(1, "world") shouldEqual 1
            value shouldEqual "hworldllo world"
            undo()
            value shouldEqual "hallo world"
            undo()
            value shouldEqual "hallo"
            undo()
            value shouldEqual "hello"
        }
    }

    @Test
    fun undo() {
        editor.run {
            insert("hello")
            undo()
            value shouldEqual ""
            cursor shouldEqual 0
            canUndo() shouldEqual false
            canRedo() shouldEqual true
        }
    }

    @Test
    fun selection() {
        editor.run {
            insert("hello world")
            select(0..5)
            selection shouldEqual 0..5
            insert("!")
            value shouldEqual "!world"
            selection shouldEqual null
            undo()
            value shouldEqual "hello world"
            selection shouldEqual null
            redo()
            value shouldEqual "!world"
            selection shouldEqual null
            insert("[]")
            value shouldEqual "![]world"
        }
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
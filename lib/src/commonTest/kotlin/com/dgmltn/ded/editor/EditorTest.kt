package com.dgmltn.ded.editor

import androidx.compose.runtime.TestOnly
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.Test

internal class EditorTest {

    var editor: LoggingEditor = LoggingEditor(StringBuilderEditor())

    @BeforeTest
    fun setup() {
        editor = LoggingEditor(StringBuilderEditor())
    }

    @AfterTest
    fun after() {
        editor.printStats()
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
    fun initial_value() {
        editor = LoggingEditor(StringBuilderEditor("initial"))
        editor.run {
            value shouldEqual "initial"
            canUndo() shouldEqual false
            canRedo() shouldEqual false
            cursor shouldEqual 0
            moveTo(7)
            insert(" inserted")
            value shouldEqual "initial inserted"
            canUndo() shouldEqual true
            canRedo() shouldEqual false
            undo()
            value shouldEqual "initial"
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
            moveTo(1)
            cursor shouldEqual 0
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
    fun getSubstring() {
        editor.run {
            insert("hello\nworld\nline3\nline4")
            getSubstring(0..4) shouldEqual "hello"
            getSubstring(6..10) shouldEqual "world"
            getSubstring(12..16) shouldEqual "line3"
            getSubstring(18..22) shouldEqual "line4"
            getSubstring(18 .. 25) shouldEqual "line4"
        }
    }

    @Test
    fun lineCount() {
        editor.run {
            rowCount shouldEqual 1
            insert("hello world")
            value shouldEqual "hello world"
            rowCount shouldEqual 1
            insert("\n")
            rowCount shouldEqual 2
            insert("line #2")
            rowCount shouldEqual 2
            insert("\nline #3\n")
            rowCount shouldEqual 4
        }
    }

    @Test
    fun lines() {
        editor.run {
            insert("hello world")
            value shouldEqual "hello world"

            getRangeOfRow(0) shouldEqual 0 .. 10

            insert("\n")
            insert("line #2")
            insert("\nline #3\n")

            getRangeOfRow(0) shouldEqual 0 .. 11
            getRangeOfRow(1) shouldEqual 12 .. 19
            getRangeOfRow(2) shouldEqual 20 .. 27
        }
    }

    @Test
    fun getLine() {
        editor.run {
            insert("hello\nworld\nline3\n\nline4\n")
            value shouldEqual "hello\nworld\nline3\n\nline4\n"
            rowCount shouldEqual 6
            getRangeOfRow(0) shouldEqual 0..5
            getRangeOfRow(1) shouldEqual 6..11
            getRangeOfRow(3) shouldEqual 18..18
            getRangeOfRow(4) shouldEqual 19..24
            getRangeOfRow(5) shouldEqual 25..25
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
    fun getRowOf() {
        editor.run {
            getRowOf(0) shouldEqual 0
            insert("foo\nbar\nbaz")
            getRowOf(3) shouldEqual 0
            getRowOf(4) shouldEqual 1
            getRowOf(10) shouldEqual 2
            insert("\n")
            getRowOf(11) shouldEqual 2
            getRowOf(12) shouldEqual 3
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
            getRangeOfRow(1) shouldEqual 6..6
            insert("world\nline3")
            getRangeOfRow(1) shouldEqual 6..11
            getRangeOfRow(2) shouldEqual 12..16
            insert("\n")
            getRangeOfRow(3) shouldEqual 18..18
        }
    }

    @Test
    fun getRangeOfToken() {
        editor.run {
            getRangeOfToken(0) shouldEqual IntRange.EMPTY
            insert("hello world")
            getRangeOfToken(0) shouldEqual 0..4
            getRangeOfToken(1) shouldEqual 0..4
            getRangeOfToken(4) shouldEqual 0..4
            getRangeOfToken(5) shouldEqual 5..5
            getRangeOfToken(6) shouldEqual 6..10
            getRangeOfToken(7) shouldEqual 6..10
            getRangeOfToken(100) shouldEqual IntRange.EMPTY
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
    fun backspace() {
        editor.run {
            cursor shouldEqual 0
            backspace(1) shouldEqual 0
            insert("hello")
            cursor shouldEqual 5
            backspace(1) shouldEqual 1
            value shouldEqual "hell"
            cursor shouldEqual 4
            backspace(1) shouldEqual 1
            value shouldEqual "hel"
            cursor shouldEqual 3
            backspace(3) shouldEqual 3
            value shouldEqual ""
            cursor shouldEqual 0
            backspace(5) shouldEqual 0
            cursor shouldEqual 0
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
    fun empty_value() {
        editor.run {
            value shouldEqual ""
            cursor shouldEqual 0
            insert("test")
            value shouldEqual "test"
            cursor shouldEqual 4
            undo()
            value shouldEqual ""
            cursor shouldEqual 0
            undo()
            value shouldEqual ""
            cursor shouldEqual 0
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

    @Test
    fun moveByInt() {
        editor.run {
            cursor shouldEqual 0
            moveBy(10) shouldEqual 0
            cursor shouldEqual 0
            moveBy(-10) shouldEqual 0
            cursor shouldEqual 0

            insert("hello world\nline 2\nline 3")
            cursor shouldEqual 25

            moveTo(0)
            cursor shouldEqual 0
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 0

            moveBy(1)
            cursor shouldEqual 1
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 1

            moveBy(10) shouldEqual 11
            cursor shouldEqual 11
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 11

            moveBy(13) shouldEqual 24
            cursor shouldEqual 24
            getRowOfCursor() shouldEqual 2
            getColOfCursor() shouldEqual 5

            // Special case: last character
            moveBy(1) shouldEqual 25
            cursor shouldEqual 25
            getRowOfCursor() shouldEqual 2
            getColOfCursor() shouldEqual 6

            moveBy(-14)
            cursor shouldEqual 11
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 11

            moveBy(-10)
            cursor shouldEqual 1
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 1
        }
    }

    @Test
    fun moveByRowCol() {
        editor.run {
            cursor shouldEqual 0
            moveBy(RowCol(1, 0)) shouldEqual 0
            moveBy(RowCol(-1, 0)) shouldEqual 0
            moveBy(RowCol(0, 1)) shouldEqual 0
            moveBy(RowCol(0, -1)) shouldEqual 0

            insert("hello world\nline 2\nline 3")
            cursor shouldEqual 25

            moveTo(0)
            cursor shouldEqual 0
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 0

            moveBy(RowCol(1, 0))
            cursor shouldEqual 12
            getRowOfCursor() shouldEqual 1
            getColOfCursor() shouldEqual 0

            moveTo(0)
            moveBy(RowCol(0, 100))
            cursor shouldEqual 11
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 11

            moveTo(0)
            moveBy(RowCol(2, 1))
            cursor shouldEqual 20
            getRowOfCursor() shouldEqual 2
            getColOfCursor() shouldEqual 1

            moveBy(RowCol(-1, 2))
            cursor shouldEqual 15
            getRowOfCursor() shouldEqual 1
            getColOfCursor() shouldEqual 3
        }
    }

    @Test
    fun moveToRowCol() {
        editor.run {
            cursor shouldEqual 0
            moveTo(RowCol(10, 10)) shouldEqual 0
            cursor shouldEqual 0

            insert("hello world\nline 2\nline 3")
            cursor shouldEqual 25

            moveTo(RowCol(0, 0))
            cursor shouldEqual 0
            getRowOfCursor() shouldEqual 0
            getColOfCursor() shouldEqual 0

            moveTo(RowCol(2, 2)) shouldEqual 21

            // Special case
            moveTo(RowCol(10, 0)) shouldEqual 25
            moveTo(RowCol(-1, 10)) shouldEqual 0
            moveTo(RowCol(2, 20)) shouldEqual 25
        }
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
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
        editor.move(0)
        editor.insert("hello world")
        editor.value shouldEqual "hello world"
        editor.cursor shouldEqual 11

        // Insert
        editor.move(5)
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
        editor.move(5)
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

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
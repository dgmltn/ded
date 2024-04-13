package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Length
import com.dgmltn.ded.fredbuf.redblacktree.Line
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.measureTime

internal class TreeTest {

    companion object {
        const val HELLO_WORLD = "hello world"
    }

    @BeforeTest
    fun setup() {
    }


    @Test
    fun test0_Basic_TreeBuilder() {
        val builder = TreeBuilder()
        builder.accept("foo\nbar\nbaz")
        val tree = builder.create()
        tree shouldEqual "foo\nbar\nbaz"
        tree.lineCount().value shouldEqual 3
        tree.root.treeLength().value shouldEqual 11
        tree.meta.totalContentLength.value shouldEqual 11
        tree.meta.lfCount.value shouldEqual 2
        tree.isEmpty() shouldEqual false
        tree.length().value shouldEqual 11
    }

    @Test
    fun test1_() {
        val builder = TreeBuilder()
        builder.accept("a\nb\nc\nd")
        val tree = builder.create()
        tree.remove(CharOffset(4), Length(1))
        tree.remove(CharOffset(3), Length(1))
        tree shouldEqual "a\nb\nd"
        tree.lineCount().value shouldEqual 3
    }

    /**
     * -------------------------
     * INSERTION + REMOVAL TESTS
     * -------------------------
     */

    @Test
    fun test_0_insert_basic() {
        val hello = "hello world"
        val tree = Tree()
        tree.insert(CharOffset(0), hello)
        tree.lineCount().value shouldEqual 1
        tree.getLineContent(Line(1)) shouldEqual hello
        tree.root.treeLength().value shouldEqual 11
    }

    @Test
    fun test_1_insert_basic_with_newlines() {
        val hello = "foo\nbar\nbaz"
        val tree = Tree()
        tree.insert(CharOffset(0), hello)
//        tree.line_count().value shouldEqual 3
        tree.getLineContent(Line(1)) shouldEqual "foo"
        tree.getLineContent(Line(2)) shouldEqual "bar"
        tree.getLineContent(Line(3)) shouldEqual "baz"
        tree.root.treeLength().value shouldEqual 11
    }


    @Test
    fun test_1_insert_newline() {
        val tree = Tree()
        tree.insert(CharOffset(0), HELLO_WORLD)
        tree.insert(CharOffset(5), "\n")
        tree.lineCount().value shouldEqual 2
        tree.root.treeLength().value shouldEqual 12
//        tree.get_line_range(Line(0)) shouldEqual LineRange(CharOffset(0), CharOffset(4))
        tree.getLineContent(Line(1)) shouldEqual "hello"
        tree.getLineContent(Line(2)) shouldEqual " world"
    }

    @Test
    fun test_2_insert_more() {
        val tree = Tree()
        tree.insert(CharOffset(0), HELLO_WORLD)
        tree.insert(CharOffset(5), "\n")
        tree.insert(CharOffset(13), "\nanother\nnewline")
        tree.lineCount().value shouldEqual 4
    }

//    @Test
//    fun test_1_insertion_removal_small() = measure {
//        repeat(100) {
//            textEditor.insert(HELLO_WORLD)
//            textEditor.delete(5)
//        }
//    }
//
//    @Test
//    fun test_2_insertion_removal_medium() = measure {
//        repeat(10_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        textEditor.delete(50_000)
//    }
//
//    @Test
//    fun test_3_insertion_removal_huge() = measure {
//        repeat(100_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        textEditor.delete(5_000_000)
//    }


    /**
     * -----------------
     * MOVE CURSOR TESTS
     * -----------------
     */


//    @Test
//    fun test_4_cursor_movement_small() {
//        textEditor.insert(HELLO_WORLD)
//        measure {
//            textEditor.moveCursorToStart()
//            textEditor.moveCursorToEnd()
//            textEditor.moveCursorToStart()
//        }
//    }
//
//    @Test
//    fun test_5_cursor_movement_medium() {
//        repeat(10_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        measure {
//            textEditor.moveCursorToStart()
//            textEditor.moveCursorToEnd()
//            textEditor.moveCursorToStart()
//        }
//    }
//
//    @Test
//    fun test_6_cursor_movement_huge() {
//        repeat(100_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        measure {
//            textEditor.moveCursorToStart()
//            textEditor.moveCursorToEnd()
//            textEditor.moveCursorToStart()
//        }
//    }


    /**
     * ---------------------------------
     * INSERT, MOVE CURSOR, INSERT TESTS
     * ---------------------------------
     */


//    @Test
//    fun test_7_move_cursor_and_insert_small() {
//        textEditor.insert(HELLO_WORLD)
//        measure {
//            textEditor.moveCursor(position = 0)
//            textEditor.insert(HELLO_WORLD)
//            textEditor.moveCursor(position = HELLO_WORLD.length)
//            textEditor.insert(HELLO_WORLD)
//        }
//    }
//
//    @Test
//    fun test_8_move_cursor_and_insert_medium() {
//        repeat(10_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        measure {
//            textEditor.moveCursorToStart()
//            textEditor.insert(HELLO_WORLD)
//            textEditor.moveCursorToEnd()
//            textEditor.insert(HELLO_WORLD)
//            textEditor.moveCursor(HELLO_WORLD.length * 5_000) // half
//            textEditor.insert(HELLO_WORLD)
//        }
//    }
//
//    @Test
//    fun test_9_move_cursor_and_insert_huge() {
//        repeat(100_000) {
//            textEditor.insert(HELLO_WORLD)
//        }
//        measure {
//            textEditor.moveCursor(0) // start
//            textEditor.insert(HELLO_WORLD)
//            textEditor.moveCursor(HELLO_WORLD.length * 100_000) // end
//            textEditor.insert(HELLO_WORLD)
//            textEditor.moveCursor(HELLO_WORLD.length * 50_000) // half
//            textEditor.insert(HELLO_WORLD)
//        }
//    }

    private fun measure(block: ()->Unit) {
        val time = measureTime(block)
        println("Execution took: ${time}")
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }

    private infix fun Tree.shouldEqual(expected: String) {
        val start = CharOffset(0)
        val walker = TreeWalker(this, start)
        val sb = StringBuilder()
        while (!walker.exhausted()) {
            sb.append(walker.next())
        }
        assertEquals(0, walker.remaining().value)
        val buf = sb.toString()

        assertEquals(expected, buf)

        //assume_buffer_snapshots(tree, expected, start, locus);
        //assume_reverse_buffer(tree, buf, start + retract(tree->length()), locus);
    }

}
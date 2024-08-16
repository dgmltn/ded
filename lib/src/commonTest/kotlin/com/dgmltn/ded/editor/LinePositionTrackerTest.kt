package com.dgmltn.ded.editor

import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.BeforeTest

class LinePositionTrackerTest {

    val builder: StringBuilder = StringBuilder()
    var tracker: LinePositionTracker = LinePositionTracker(builder)

    @BeforeTest
    fun setup() {
        builder.clear()
        tracker = LinePositionTracker(builder)
    }

    @Test
    fun newline() {
        tracker.run {
            builder.append("foo\nbar\nbaz\nabc\ndef")
            invalidateIndices(0)
            addNewlines(4)
            newline(0) shouldEqual 3
            newline(1) shouldEqual 7
            newline(2) shouldEqual 11
            newline(3) shouldEqual 15
        }
    }

    @Test
    fun newline_2() {
        tracker.run {
            builder.append("foo\nbar\nbaz\nabc\ndef")
            invalidateIndices(0)
            addNewlines(4)
            newline(2) shouldEqual 11
            newline(3) shouldEqual 15
            newline(1) shouldEqual 7
            newline(0) shouldEqual 3
        }
    }

    @Test
    fun newline_3() {
        tracker.run {
            builder.append("foo\nbar\nbaz\nabc\n")
            invalidateIndices(0)
            addNewlines(4)
            newline(2) shouldEqual 11
            newline(3) shouldEqual 15
            newline(1) shouldEqual 7
            newline(0) shouldEqual 3
        }
    }

    @Test
    fun getRowOf() {
        tracker.run {
            getRowOf(0) shouldEqual 0

            builder.append("hello\ntesting\n1\n\n2\n3\ntest")
            invalidateIndices(0)
            addNewlines(6)

            // Edge case
            getRowOf(0) shouldEqual 0
            getRowOf(1) shouldEqual 0
            getRowOf(4) shouldEqual 0
            getRowOf(5) shouldEqual 0
            getRowOf(6) shouldEqual 1
            getRowOf(13) shouldEqual 1
            getRowOf(24) shouldEqual 6
            getRowOf(25) shouldEqual 6
        }
    }

    @Test
    fun case_2() {
        tracker.run {
            builder.append("foo\nbar\nbaz")
            invalidateIndices(0)
            addNewlines(2)
            getRowOf(3) shouldEqual 0
            getRowOf(4) shouldEqual 1
            getRowOf(10) shouldEqual 2
            builder.append("\n")
            invalidateIndices(2)
            addNewlines(1)
            getRowOf(11) shouldEqual 2
            getRowOf(12) shouldEqual 3
        }
    }

    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
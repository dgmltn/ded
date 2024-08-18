package com.dgmltn.ded.editor

import kotlin.time.Duration
import kotlin.time.measureTimedValue

class LoggingEditor(private val child: Editor): Editor {

    val counts = mutableMapOf<String, Int>()
    val timings = mutableMapOf<String, Duration>()

    private fun <T> timeMe(f: () -> T): T {
        val (ret, duration) = measureTimedValue { f() }
        val e = Exception().stackTrace
        val methodName = e[1].methodName
        timings[methodName] = timings[methodName]?.plus(duration) ?: duration
        counts[methodName] = counts[methodName]?.plus(1) ?: 1
        return ret
    }

    fun printStats() {
        println("\n\n# Stats:\n")
        timings.keys.sortedBy { timings[it] }.forEach {
            val t = timings[it]!!
            val c = counts[it]!!
            println("$it: $c * (${t.div(c)}) = $t")
        }
    }

    override var cursor: Int
        get() = timeMe { child.cursor }
        set(value) {
            timeMe { child.cursor = value }
        }

    override var cursorRow: Int?
        get() = timeMe { child.cursorRow }
        set(value) { child.cursorRow = value }

    override var cursorCol: Int?
        get() = timeMe { child.cursorCol }
        set(value) { child.cursorRow = value }

    override var selection: IntProgression?
        get() = timeMe { child.selection }
        set(value) {
            timeMe { child.selection = value }
        }

    override val value: String
        get() = timeMe { child.value }

    override val rowCount: Int
        get() = timeMe { child.rowCount }

    override val length: Int
        get() = timeMe { child.length }

    override fun insert(value: String) =
        timeMe { child.insert(value) }

    override fun delete(count: Int) =
        timeMe { child.delete(count) }

    override fun backspace(count: Int) =
        timeMe { child.backspace(count) }

    override fun replace(count: Int, value: String) =
        timeMe { child.replace(count, value) }

    override fun canUndo() =
        timeMe { child.canUndo() }

    override fun undo() =
        timeMe { child.undo() }

    override fun canRedo() =
        timeMe { child.canRedo() }

    override fun redo() =
        timeMe { child.redo() }

    override fun getCharAt(position: Int) =
        timeMe { child.getCharAt(position) }

    override fun getSubstring(startPosition: Int, endPosition: Int) =
        timeMe { child.getSubstring(startPosition, endPosition) }

    override fun getRangeOfRow(row: Int) =
        timeMe { child.getRangeOfRow(row) }

    override fun moveTo(position: Int) =
        timeMe { child.moveTo(position) }

    override fun moveTo(rowCol: RowCol) =
        timeMe { child.moveTo(rowCol) }

    override fun moveBy(delta: Int) =
        timeMe { child.moveBy(delta) }

    override fun moveBy(rowColDelta: RowCol) =
        timeMe { child.moveBy(rowColDelta) }

    override fun select(range: IntProgression) {
        timeMe { child.select(range) }
    }

    override fun getRangeOfToken(position: Int, isIdentifierChar: (Char) -> Boolean) =
        timeMe { child.getRangeOfToken(position, isIdentifierChar) }

    override fun getSubstring(range: IntRange) =
        timeMe { child.getSubstring(range) }

    override fun getLastRow() =
        timeMe { child.getLastRow() }

    override fun getRowOf(position: Int) =
        timeMe { child.getRowOf(position) }

    override fun getRowOfCursor() =
        timeMe { child.getRowOfCursor() }

    override fun getColOfCursor() =
        timeMe { child.getColOfCursor() }
}
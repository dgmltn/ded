package com.dgmltn.ded.fredbuf.redblacktree

import com.dgmltn.ded.fredbuf.editor.Column
import com.dgmltn.ded.fredbuf.editor.Length


data class BufferCursor(
    val line: Line = Line(), // Relative line in the current buffer.
    val column: Column = Column() // Column into the current line.
) {
    override fun toString(): String = "[${line.value},${column.value}]"
}


data class BufferIndex(val value: Int = 0) {
    companion object {
        val ModBuf = BufferIndex(-1)
    }

    override fun toString(): String =
        if (this == ModBuf) "ModBuf" else value.toString()
}


data class LFCount(val value: Int = 0) {
    operator fun plus(other: LFCount) = LFCount(this.value + other.value)
    override fun toString(): String = value.toString()
}

data class Line(val value: Int = 0) {
    companion object {
        val IndexBeginning = Line(-1)
        val Beginning = Line(-2)
    }

    operator fun plus(other: Int) = Line(value + other)
    operator fun minus(other: Int) = Line(value - other)

    override fun toString(): String =
        when {
            this == IndexBeginning -> "IndexBeginning"
            this == Beginning -> "Beginning"
            else -> value.toString()
        }
}

data class Piece(
    val index: BufferIndex, // Index into a buffer in PieceTree. This could be an immutable buffer or the mutable buffer.
    val first: BufferCursor,
    val last: BufferCursor,
    val length: Length,
    val newlineCount: LFCount
): Comparable<Piece> {
    override fun compareTo(other: Piece): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String =
        "Piece: $first-$last (len=$length) ($newlineCount newlines) (bufferIndex: $index)"
}
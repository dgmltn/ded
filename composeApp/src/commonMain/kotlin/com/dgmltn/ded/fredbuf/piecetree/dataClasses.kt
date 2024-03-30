package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.CharOffset
import com.dgmltn.ded.fredbuf.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferCursor
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.LFCount
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.Piece
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree

data class UndoRedoEntry(val root: RedBlackTree, val op_offset: CharOffset)

typealias Accumulator = (collection: BufferCollection, piece: Piece, line: Line) -> Length

data class LineStart(val value: Int = 0)

typealias LineStarts = MutableList<LineStart>

data class NodePosition(
    val node: RedBlackTree.NodeData? = null,
    val remainder: Length = Length(),
    val start_offset: CharOffset = CharOffset(),
    val line: Line = Line()
)

class CharBuffer(
    var buffer: String = "",
    val line_starts: LineStarts = mutableListOf()
)

typealias BufferReference = CharBuffer

typealias Buffers = List<BufferReference>

data class BufferCollection(
    val orig_buffers: Buffers = listOf(),
    val mod_buffer: CharBuffer = CharBuffer()
) {
    fun buffer_offset(index: BufferIndex, cursor: BufferCursor): CharOffset {
        val starts = buffer_at(index).line_starts
        return CharOffset(starts[cursor.line.value].value + cursor.column.value)
    }

    fun buffer_at(index: BufferIndex): CharBuffer {
        if (index == BufferIndex.ModBuf)
            return mod_buffer
        return orig_buffers[index.value]
    }
}

data class LineRange(
    val first: CharOffset,
    val last: CharOffset  // Does not include LF.
)

data class UndoRedoResult(
    val success: Boolean,
    val op_offset: CharOffset
)

// When mutating the tree nodes are saved by default into the undo stack.  This
// allows callers to suppress this behavior.
enum class SuppressHistory { No, Yes }

data class BufferMeta(
    val lf_count: LFCount = LFCount(),
    val total_content_length: Length = Length()
)

// Indicates whether or not line was missing a CR (e.g. only a '\n' was at the end).
enum class IncompleteCRLF { No, Yes }


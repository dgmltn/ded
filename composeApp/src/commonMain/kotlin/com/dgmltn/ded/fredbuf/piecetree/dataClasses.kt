package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferCursor
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.LFCount
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.Piece
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree

data class UndoRedoEntry(val root: RedBlackTree, val opOffset: CharOffset)

typealias Accumulator = (collection: BufferCollection, piece: Piece, line: Line) -> Length

data class LineStart(val value: Int = 0)

typealias LineStarts = MutableList<LineStart>

data class NodePosition(
    val node: RedBlackTree.NodeData? = null,
    val remainder: Length = Length(),
    val startOffset: CharOffset = CharOffset(),
    val line: Line = Line()
)

class CharBuffer(
    var buffer: String = "",
    val lineStarts: LineStarts = mutableListOf()
)

typealias BufferReference = CharBuffer

typealias Buffers = List<BufferReference>

data class BufferCollection(
    val origBuffers: Buffers = listOf(),
    val modBuffer: CharBuffer = CharBuffer()
) {
    fun bufferOffset(index: BufferIndex, cursor: BufferCursor): CharOffset {
        val starts = bufferAt(index).lineStarts
        return CharOffset(starts[cursor.line.value].value + cursor.column.value)
    }

    fun bufferAt(index: BufferIndex): CharBuffer {
        if (index == BufferIndex.ModBuf)
            return modBuffer
        return origBuffers[index.value]
    }
}

data class LineRange(
    val first: CharOffset,
    val last: CharOffset  // Does not include LF.
)

data class UndoRedoResult(
    val success: Boolean,
    val opOffset: CharOffset
)

// When mutating the tree nodes are saved by default into the undo stack.  This
// allows callers to suppress this behavior.
enum class SuppressHistory { No, Yes }

data class BufferMeta(
    val lfCount: LFCount = LFCount(),
    val totalContentLength: Length = Length()
)

// Indicates whether or not line was missing a CR (e.g. only a '\n' was at the end).
enum class IncompleteCRLF { No, Yes }


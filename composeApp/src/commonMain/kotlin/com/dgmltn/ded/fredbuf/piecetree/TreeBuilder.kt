package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.piecetree.Tree.Companion.populate_line_starts

class TreeBuilder {
    val buffers = mutableListOf<BufferReference>()

    fun accept(txt: String) {
        val scratch_starts = populate_line_starts(txt)
        val buffer_reference = BufferReference(txt, scratch_starts)
        buffers.add(buffer_reference)
    }

    fun create(): Tree {
        return Tree(buffers)
    }
}
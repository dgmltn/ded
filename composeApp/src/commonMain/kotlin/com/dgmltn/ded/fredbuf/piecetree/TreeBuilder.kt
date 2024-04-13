package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.piecetree.Tree.Companion.populateLineStarts

class TreeBuilder {
    private val buffers = mutableListOf<BufferReference>()

    fun accept(txt: String) {
        val scratchStarts = populateLineStarts(txt)
        val bufferReference = BufferReference(txt, scratchStarts)
        buffers.add(bufferReference)
    }

    fun create(): Tree {
        return Tree(buffers)
    }
}
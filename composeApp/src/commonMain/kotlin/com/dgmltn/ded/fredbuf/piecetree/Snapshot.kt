package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Length
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.Piece
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree2

interface ISnapshot {
    val tree: Tree
    var root: RedBlackTree2<Piece>
    var meta: BufferMeta
    var buffers: BufferCollection

    fun getLineContent(line: Line): String
    fun getLineContentCrlf(buf: StringBuilder, line: Line): IncompleteCRLF
    fun lineAt(offset: CharOffset): Line
    fun getLineRange(line: Line): LineRange
    fun getLineRangeCrlf(line: Line): LineRange
    fun getLineRangeWithNewline(line: Line): LineRange
    fun isEmpty(): Boolean
    fun lineCount(): Length
}

// Owning snapshot owns its own buffer data (performs a lightweight copy) so
// that even if the original tree is destroyed, the owning snapshot can still
// reference the underlying text.
class OwningSnapshot: ISnapshot {
    override val tree: Tree
    override var root: RedBlackTree
    override var meta: BufferMeta
    // This should be fairly lightweight.  The original buffers
    // will retain the majority of the memory consumption.
    override var buffers: BufferCollection

    constructor(tree: Tree) {
        this.tree = tree
        root = tree.root
        meta = tree.meta
        buffers = tree.buffers
    }

    constructor(tree: Tree, dt: RedBlackTree) {
        this.tree = tree
        root = tree.root
        buffers = tree.buffers
        meta = dt.computeBufferMeta()
    }

    // Queries.
    override fun getLineContent(line: Line): String {
        val sb = StringBuilder()

        if (line == Line.IndexBeginning)
            return sb.toString()

        if (root.isEmpty())
            return sb.toString()

        val lineOffset = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line)
        val walker = TreeWalker(this, lineOffset)
        while (!walker.exhausted()) {
            val c = walker.next()
            if (c == '\n')
                break
            sb.append(c)
        }

        return sb.toString()
    }

    override fun getLineContentCrlf(buf: StringBuilder, line: Line): IncompleteCRLF {
        if (line == Line.IndexBeginning)
            return IncompleteCRLF.No
        val node = root
        if (node.isEmpty())
            return IncompleteCRLF.No
        // Trying this new logic for now.
        val line_offset = CharOffset()
        Tree.lineStart(tree::accumulateValue, line_offset, buffers, node, line)
        return trimCrlf(buf, TreeWalker(this, line_offset))
    }

    override fun lineAt(offset: CharOffset): Line {
        if (isEmpty())
            return Line.Beginning
        val result = tree.nodeAt(buffers, root, offset)
        return result.line
    }

    override fun getLineRange(line: Line): LineRange = LineRange(
        first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
        last = Tree.lineStart(tree::accumulateValueNoLf, CharOffset(0), buffers, root, line + 1)
    )

    override fun getLineRangeCrlf(line: Line): LineRange =
        LineRange(
            first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
            last = tree.lineEndCrlf(CharOffset(0), buffers, root, root, line + 1)
        )

    override fun getLineRangeWithNewline(line: Line): LineRange =
        LineRange(
            first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
            last = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line + 1)
        )

    override fun isEmpty(): Boolean {
        return meta.totalContentLength.value == 0
    }

    override fun lineCount(): Length {
        return Length(meta.lfCount.value + 1)
    }
}

// Reference snapshot owns no data and is only valid for as long as the original
// tree buffers are valid.
// TODO: this is pretty much a copy of OwningSnapshot. Maybe just delete this one
class ReferenceSnapshot: ISnapshot {
    override val tree: Tree
    override var root: RedBlackTree
    override var meta: BufferMeta

    // A reference to the underlying tree buffers.
    override var buffers: BufferCollection

    constructor(tree: Tree) {
        this.tree = tree
        root = tree.root
        meta = tree.meta
        buffers = tree.buffers
    }

    constructor(tree: Tree, dt: RedBlackTree) {
        this.tree = tree
        root = dt
        buffers = tree.buffers
        // Compute the buffer meta for 'dt'.
        meta = dt.computeBufferMeta()
    }

    // Queries.
    override fun getLineContent(line: Line): String {
        val sb = StringBuilder()

        if (line == Line.IndexBeginning)
            return sb.toString()

        if (root.isEmpty())
            return sb.toString()

        val line_offset = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line)
        val walker = TreeWalker(this, line_offset)
        while (!walker.exhausted()) {
            val c = walker.next()
            if (c == '\n')
                break
            sb.append(c)
        }

        return sb.toString()
    }

    override fun getLineContentCrlf(buf: StringBuilder, line: Line): IncompleteCRLF {
        if (line == Line.IndexBeginning)
            return IncompleteCRLF.No
        val node = root
        if (node.isEmpty())
            return IncompleteCRLF.No
        // Trying this new logic for now.
        val line_offset = CharOffset()
        Tree.lineStart(tree::accumulateValue, line_offset, buffers, node, line)
        return trimCrlf(buf, TreeWalker(this, line_offset))
    }

    override fun lineAt(offset: CharOffset): Line {
        if (isEmpty())
            return Line.Beginning
        val result = tree.nodeAt(buffers, root, offset)
        return result.line
    }

    override fun getLineRange(line: Line): LineRange =
        LineRange(
            first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
            last = Tree.lineStart(tree::accumulateValueNoLf, CharOffset(0), buffers, root, line + 1)
        )

    override fun getLineRangeCrlf(line: Line): LineRange =
        LineRange(
            first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
            last = tree.lineEndCrlf(CharOffset(0), buffers, root, root, line + 1)
        )

    override fun getLineRangeWithNewline(line: Line): LineRange =
        LineRange(
            first = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line),
            last = Tree.lineStart(tree::accumulateValue, CharOffset(0), buffers, root, line + 1)
        )

    override fun isEmpty(): Boolean {
        return meta.totalContentLength.value == 0
    }

    override fun lineCount(): Length {
        return Length(meta.lfCount.value + 1)
    }
}

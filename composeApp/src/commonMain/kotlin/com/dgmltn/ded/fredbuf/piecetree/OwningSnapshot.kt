package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.CharOffset
import com.dgmltn.ded.fredbuf.Length
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree

// Owning snapshot owns its own buffer data (performs a lightweight copy) so
// that even if the original tree is destroyed, the owning snapshot can still
// reference the underlying text.
class OwningSnapshot {
    val tree: Tree
    var root: RedBlackTree
    var meta: BufferMeta
    // This should be fairly lightweight.  The original buffers
    // will retain the majority of the memory consumption.
    var buffers: BufferCollection

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
        meta = dt.compute_buffer_meta()
    }

    // Queries.
    fun get_line_content(line: Line): String {
        val sb = StringBuilder()

        if (line == Line.IndexBeginning)
            return sb.toString()

        if (root.isEmpty())
            return sb.toString()

        val line_offset =
            Tree.line_start(tree::accumulate_value, CharOffset(0), buffers, root, line)
        val walker = TreeWalker(this, line_offset)
        while (!walker.exhausted()) {
            val c = walker.next()
            if (c == '\n')
                break
            sb.append(c)
        }

        return sb.toString()
    }

    fun get_line_content_crlf(buf: StringBuilder, line: Line): IncompleteCRLF {
        if (line == Line.IndexBeginning)
            return IncompleteCRLF.No
        val node = root
        if (node.isEmpty())
            return IncompleteCRLF.No
        // Trying this new logic for now.
        val line_offset = CharOffset()
        Tree.line_start(tree::accumulate_value, line_offset, buffers, node, line)
        return trim_crlf(buf, TreeWalker(this, line_offset))
    }

    fun line_at(offset: CharOffset): Line {
        if (is_empty())
            return Line.Beginning
        val result = tree.node_at(buffers, root, offset)
        return result.line
    }

    fun get_line_range(line: Line): LineRange = LineRange(
        first = Tree.line_start(tree::accumulate_value, CharOffset(0), buffers, root, line),
        last = Tree.line_start(tree::accumulate_value_no_lf, CharOffset(0), buffers, root, line + 1)
    )

    fun get_line_range_crlf(line: Line): LineRange =
        LineRange(
            first = Tree.line_start(tree::accumulate_value, CharOffset(0), buffers, root, line),
            last = tree.line_end_crlf(CharOffset(0), buffers, root, root, line + 1)
        )

    fun get_line_range_with_newline(line: Line): LineRange =
        LineRange(
            first = Tree.line_start(tree::accumulate_value, CharOffset(0), buffers, root, line),
            last = Tree.line_start(tree::accumulate_value, CharOffset(0), buffers, root, line + 1)
        )

    fun is_empty(): Boolean {
        return meta.total_content_length.value == 0
    }

    fun line_count(): Length {
        return Length(meta.lf_count.value + 1)
    }
}

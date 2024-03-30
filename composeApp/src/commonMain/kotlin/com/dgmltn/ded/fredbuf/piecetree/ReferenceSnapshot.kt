package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.CharOffset
import com.dgmltn.ded.fredbuf.Length
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree


// Reference snapshot owns no data and is only valid for as long as the original
// tree buffers are valid.
// TODO: this is pretty much a copy of OwningSnapshot. Maybe just delete this one
class ReferenceSnapshot {
    var root: RedBlackTree
    var meta: BufferMeta
    // A reference to the underlying tree buffers.
    var buffers: BufferCollection

    constructor(tree: Tree) {
        root = tree.root
        meta = tree.meta
        buffers = tree.buffers
    }

    constructor(tree: Tree, dt: RedBlackTree) {
        root = dt
        buffers = tree.buffers
        // Compute the buffer meta for 'dt'.
        meta = dt.compute_buffer_meta()
    }

    // Queries.
    fun get_line_content(buf: String, line: Line): String {
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

    fun get_line_range(line: Line): LineRange =
        LineRange(
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

package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.CharOffset
import com.dgmltn.ded.fredbuf.Column
import com.dgmltn.ded.fredbuf.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferCursor
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.LFCount
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.Piece
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree.Companion.satisfies_rb_invariants

class Tree(buffers: Buffers = emptyList()) {
    val buffers: BufferCollection = BufferCollection(buffers)
    var root: RedBlackTree = RedBlackTree()
    var meta: BufferMeta = BufferMeta()
    private var scratch_starts = mutableListOf<LineStart>()
    private var last_insert: BufferCursor = BufferCursor()
    // Note: This is absolute position. Initialize to nonsense value.
    private var end_last_insert: CharOffset = CharOffset.Sentinel
    private val undo_stack = mutableListOf<UndoRedoEntry>()
    private val redo_stack = mutableListOf<UndoRedoEntry>()

    init {
        build_tree()
    }

    // Interface.
    // Initialization after populating initial immutable buffers from ctor.
    fun build_tree() {
        buffers.mod_buffer.line_starts.clear()
        buffers.mod_buffer.buffer = ""

        // In order to maintain the invariant of other buffers, the mod_buffer needs a single line-start of 0.
        buffers.mod_buffer.line_starts.add(LineStart(0))

        last_insert = BufferCursor()

        val buf_count = buffers.orig_buffers.size
        var offset = CharOffset()
        (0 until buf_count).forEach { i ->
            val buf = buffers.orig_buffers[i]
            check(buf.line_starts.isEmpty())

            // If this immutable buffer is empty, we can avoid creating a piece for it altogether.
            if (buf.buffer.isNotEmpty()) {
                val last_line = Line(buf.line_starts.size - 1)
                // Create a new node that spans this buffer and retains an index to it.
                // Insert the node into the balanced tree.
                val piece = Piece(
                    index = BufferIndex(i),
                    first = BufferCursor(line = Line(0), column = Column(0)),
                    last = BufferCursor(line = last_line, column = Column(buf.buffer.length - buf.line_starts[last_line.value].value)),
                    length = Length(buf.buffer.length),
                    // Note: the number of newlines
                    newline_count = LFCount(last_line.value)
                )
                root = root.insert(RedBlackTree.NodeData(piece = piece), offset)
                offset += piece.length
            }

        }

        compute_buffer_meta()
    }

    fun insert(offset: CharOffset, txt: String, suppress_history: SuppressHistory = SuppressHistory.No) {
        if (txt.isEmpty())
            return

        // This allows us to undo blocks of code.
        if (suppress_history == SuppressHistory.No
            && (end_last_insert != offset || root.isEmpty())) {
            append_undo(root, offset)
        }

        internal_insert(offset, txt)
    }

    fun remove(offset: CharOffset, count: Length, suppress_history: SuppressHistory = SuppressHistory.No) {
        // Rule out the obvious noop.
        if (count.value == 0 || root.root_ptr == null)
            return

        if (suppress_history == SuppressHistory.No) {
            append_undo(root, offset)
        }

        internal_remove(offset, count)
    }

    fun try_undo(op_offset: CharOffset): UndoRedoResult {
        if (undo_stack.isEmpty())
            return UndoRedoResult(
                success = false,
                op_offset = CharOffset(0)
            )

        redo_stack.add(0, UndoRedoEntry(root, op_offset))
        val (node, undo_offset) = undo_stack.removeFirst()
        root = node
        compute_buffer_meta()
        return UndoRedoResult(
            success = true,
            op_offset = undo_offset
        )
    }

    fun try_redo(op_offset: CharOffset): UndoRedoResult {
        if (redo_stack.isEmpty())
            return UndoRedoResult(
                success = false,
                op_offset = CharOffset(0)
            )

        undo_stack.add(0, UndoRedoEntry(root, op_offset))
        val (node, redo_offset) = redo_stack.removeFirst()
        root = node
        compute_buffer_meta()
        return UndoRedoResult(
            success = true,
            op_offset = redo_offset
        )
    }

    // Direct history manipulation.
    // This will commit the current node to the history.  The offset provided will be the undo point later.
    fun commit_head(offset: CharOffset) {
        append_undo(root, offset)
    }

    fun head(): RedBlackTree = root

    // Snaps the tree back to the specified root.  This needs to be called with a root that is derived from
    // the set of buffers based on its creation.
    fun snap_to(new_root: RedBlackTree) {
        root = new_root
        compute_buffer_meta()
    }

    // Queries.
    fun get_line_content(line: Line): String =
        if (line == Line.IndexBeginning) ""
        else assemble_line(root, line)

    fun get_line_content_crlf(line: Line): IncompleteCRLF {
        val sb = StringBuilder()
        if (line == Line.IndexBeginning)
            return IncompleteCRLF.No
        val node = root
        if (node.isEmpty())
            return IncompleteCRLF.No
        // Trying this new logic for now.
        val line_offset = CharOffset()
        line_start(::accumulate_value, line_offset, buffers, node, line)
        return trim_crlf(sb, TreeWalker(this, line_offset))
    }

    fun at(offset: CharOffset): Char? = char_at(buffers, root, offset)

    fun line_at(offset: CharOffset): Line =
        if (is_empty()) Line.Beginning
        else node_at(buffers, root, offset).line

    fun get_line_range(line: Line): LineRange = LineRange(
        first = line_start(::accumulate_value, CharOffset(0), buffers, root, line),
        last = line_start(::accumulate_value_no_lf, CharOffset(0), buffers, root, line + 1)
    )

    fun get_line_range_crlf(line: Line): LineRange =
        LineRange(
            first = line_start(::accumulate_value, CharOffset(0), buffers, root, line),
            last = line_end_crlf(CharOffset(0), buffers, root, root, line + 1)
        )

    fun get_line_range_with_newline(line: Line): LineRange =
        LineRange(
            first = line_start(::accumulate_value, CharOffset(0), buffers, root, line),
            last = line_start(::accumulate_value, CharOffset(0), buffers, root, line + 1)
        )

    fun length(): Length = meta.total_content_length

    fun is_empty(): Boolean = meta.total_content_length.value == 0

    fun line_feed_count(): LFCount = meta.lf_count

    fun line_count(): Length = Length(line_feed_count().value + 1)

    fun owning_snap(): OwningSnapshot = OwningSnapshot(this)

    fun ref_snap(): ReferenceSnapshot = ReferenceSnapshot(this)

    // Privates
    private fun print_piece(piece: Piece, tree: Tree, level: Int) {
        TODO()
    }

    private fun print_tree(root: RedBlackTree, tree: Tree, level: Int = 0, node_offset: Int = 0) {
        TODO()
    }

    private fun print_tree(tree: Tree) {
        TODO()
    }

    private fun print_buffer(tree: Tree): String {
        val sb = StringBuilder("--- Entire Buffer ---\n")
        val walker = TreeWalker(tree)
        while (!walker.exhausted()) {
            sb.append(walker.next())
        }
        sb.append("\n")
        return sb.toString()
    }

    private fun internal_insert(offset: CharOffset, txt: String) {
        check(txt.isNotEmpty())
        end_last_insert = offset + Length(txt.length)
        compute_buffer_meta()
        root.satisfies_rb_invariants()

        if (root.isEmpty()) {
            val piece = build_piece(txt)
            root = root.insert(RedBlackTree.NodeData(piece = piece), CharOffset(0))
            return;
        }

        var result = node_at(buffers, root, offset)

        // If the offset is beyond the buffer, just select the last node.
        if (result.node == null) {
            var off = CharOffset(0)
            if (meta.total_content_length != Length()) {
                off = off + meta.total_content_length - Length(1)
            }
            result = node_at(buffers, root, off)
        }

        // There are 3 cases:
        // 1. We are inserting at the beginning of an existing node.
        // 2. We are inserting at the end of an existing node.
        // 3. We are inserting in the middle of the node.
        val node = result.node
        val remainder = result.remainder
        var node_start_offset = result.start_offset
//        val line = result.line
        check(node != null)
        val insert_pos = buffer_position(buffers, node.piece, remainder)

        // Case #1.
        if (node_start_offset == offset) {
            // There's a bonus case here.  If our last insertion point was the same as this piece's
            // last and it inserted into the mod buffer, then we can simply 'extend' this piece by
            // the following process:
            // 1. Fetch the previous node (if we can) and compare.
            // 2. Build the new piece.
            // 3. Remove the old piece.
            // 4. Extend the old piece's length to the length of the newly created piece.
            // 5. Re-insert the new piece.
            if (offset != CharOffset()) {
                val prev_node_result = node_at(buffers, root, offset - Length(1))
                if (prev_node_result.node?.piece?.index == BufferIndex.ModBuf
                    && prev_node_result.node.piece.last == last_insert) {
                    val new_piece = build_piece(txt)
                    combine_pieces(prev_node_result, new_piece)
                }
            }
            val piece = build_piece(txt)
            root.insert(RedBlackTree.NodeData(piece = piece), offset)
            return
        }

        val inside_node = offset < node_start_offset + node.piece.length

        // Case #2.
        if (!inside_node) {
            // There's a bonus case here.  If our last insertion point was the same as this piece's
            // last and it inserted into the mod buffer, then we can simply 'extend' this piece by
            // the following process:
            // 1. Build the new piece.
            // 2. Remove the old piece.
            // 3. Extend the old piece's length to the length of the newly created piece.
            // 4. Re-insert the new piece.
            if (node.piece.index == BufferIndex.ModBuf && node.piece.last == last_insert) {
                val new_piece = build_piece(txt)
                combine_pieces(result, new_piece)
                return
            }
            // Insert the new piece at the end.
            val piece = build_piece(txt)
            root.insert(RedBlackTree.NodeData(piece = piece), offset)
            return
        }

        // Case #3.
        // The basic approach here is to split the existing node into two pieces
        // and insert the new piece in between them.
        val new_len_right = buffers.buffer_offset(node.piece.index, insert_pos).distance(buffers.buffer_offset(node.piece.index, node.piece.last))
        val new_piece_right = node.piece.copy(
            first = insert_pos,
            length = new_len_right,
            newline_count = line_feed_count(buffers, node.piece.index, insert_pos, node.piece.last)
        )

        // Remove the original node tail.
        val new_piece_left = trim_piece_right(buffers, node.piece, insert_pos)

        val new_piece = build_piece(txt)

        // Remove the original node.
        root = root.remove(node_start_offset)

        // Insert the left.
        root = root.insert(RedBlackTree.NodeData(piece = new_piece_left), node_start_offset)

        // Insert the new mid.
        node_start_offset += new_piece_left.length
        root = root.insert(RedBlackTree.NodeData(piece = new_piece), node_start_offset)

        // Insert remainder.
        node_start_offset += new_piece.length
        root = root.insert(RedBlackTree.NodeData(piece = new_piece_right), node_start_offset)
    }

    private fun internal_remove(offset: CharOffset, count: Length) {
        check(count.value != 0 && !root.isEmpty())

        compute_buffer_meta()
        satisfies_rb_invariants(root)

        val first = node_at(buffers, root, offset)
        val last = node_at(buffers, root, offset + count)
        val first_node = first.node
        val last_node = last.node

        val start_split_pos = buffer_position(buffers, first_node!!.piece, first.remainder)

        // Simple case: the range of characters we want to delete are
        // held directly within this node.  Remove the node, resize it
        // then add it back.
        if (first_node == last_node) {
            val end_split_pos = buffer_position(buffers, first_node.piece, last.remainder)

            // We're going to shrink the node starting from the beginning.
            if (first.start_offset == offset) {

                // Delete the entire node.
                if (count == first_node.piece.length) {
                    root = root.remove(first.start_offset)
                    return
                }

                // Shrink the node.
                val new_piece = trim_piece_left(buffers, first_node.piece, end_split_pos)

                // Remove the old one and update.
                root = root.remove(first.start_offset)
                    .insert(RedBlackTree.NodeData(new_piece), first.start_offset)
                return
            }

            // Trim the tail of this piece.
            if (first.start_offset + first_node.piece.length == offset + count) {
                val new_piece = trim_piece_right(buffers, first_node.piece, start_split_pos)

                // Remove the old one and update.
                root = root.remove(first.start_offset)
                    .insert(RedBlackTree.NodeData(new_piece), first.start_offset)
                return
            }

            // The removed buffer is somewhere in the middle.  Trim it in both directions.
            val (left, right) = shrink_piece(buffers, first_node.piece, start_split_pos, end_split_pos)
            root = root.remove(first.start_offset)
                // Note: We insert right first so that the 'left' will be inserted
                // to the right node's left.
                .insert(RedBlackTree.NodeData(right), first.start_offset)
                .insert(RedBlackTree.NodeData(left), first.start_offset)
            return
        }

        // Traverse nodes and delete all nodes within the offset range. First we will build the
        // partial pieces for the nodes that will eventually make up this range.
        // There are four cases here:
        // 1. The entire first node is deleted as well as all of the last node.
        // 2. Part of the first node is deleted and all of the last node.
        // 3. Part of the first node is deleted and part of the last node.
        // 4. The entire first node is deleted and part of the last node.

        val new_first = trim_piece_right(buffers, first_node.piece, start_split_pos)
        if (last_node == null) {
            remove_node_range(first, count)
        }
        else {
            val end_split_pos = buffer_position(buffers, last_node.piece, last.remainder)
            val new_last = trim_piece_left(buffers, last_node.piece, end_split_pos)
            remove_node_range(first, count)

            // There's an edge case here where we delete all the nodes up to 'last' but
            // last itself remains untouched.  The test of 'remainder' in 'last' can identify
            // this scenario to avoid inserting a duplicate of 'last'.
            if (last.remainder.value != 0) {
                if (new_last.length.value != 0) {
                    root = root.insert(RedBlackTree.NodeData(new_last), first.start_offset)
                }
            }
        }

        if (new_first.length.value != 0) {
            root = root.insert(RedBlackTree.NodeData(new_first), first.start_offset)
        }
    }

    fun line_end_crlf(offset: CharOffset, buffers: BufferCollection, root: RedBlackTree, node: RedBlackTree, line: Line): CharOffset {
        if (node.isEmpty())
            return offset

        check(line != Line.IndexBeginning)
        var line_index = line.value - 1
        if (node.root().left_subtree_lf_count.value >= line_index) {
            line_end_crlf(offset, buffers, root, node.left(), line)
        }

        // The desired line is directly within the node.
        else if ((node.root().left_subtree_lf_count + node.root().piece.newline_count).value >= line_index) {
            line_index -= node.root().left_subtree_lf_count.value
            var len = node.root().left_subtree_length
            if (line_index != 0) {
                len = len + accumulate_value_no_lf(buffers, node.root().piece, Line(line_index - 1))
            }

            // If the length is anything but 0, we need to check if the last character was a carriage return.
            if (len != Length()) {
                val last_char_offset = offset.value + len.value - 1
                if (char_at(buffers, root, CharOffset(last_char_offset)) == '\r' && char_at(buffers, root, CharOffset(last_char_offset + 1)) == '\n') {
                    len = Length(len.value - 1)
                }
            }
            return offset + len
        }

        // assemble the LHS and RHS.
        else {
            // This case implies that 'left_subtree_lf_count + piece NL count' is strictly < line_index.
            // The content is somewhere in the middle.
            val piece = node.root().piece
            line_index -= node.root().left_subtree_lf_count.value + piece.newline_count.value
            val new_offset = offset + node.root().left_subtree_length + piece.length
            return line_end_crlf(new_offset, buffers, root, node.right(), Line(line_index + 1))
        }
    }

    // Fetches the length of the piece starting from the first line to 'index' or to the end of
    // the piece.
    fun accumulate_value(buffers: BufferCollection, piece: Piece, index: Line): Length {
        val buffer = buffers.buffer_at(piece.index)
        val line_starts = buffer.line_starts
        // Extend it so we can capture the entire line content including newline.
        val expected_start = piece.first.line + index.value + 1
        val first = line_starts[piece.first.line.value].value + piece.first.column.value
        if (expected_start.value > piece.last.line.value) {
            val last = line_starts[piece.last.line.value].value + piece.last.column.value
            return Length(last - first)
        }
        val last = line_starts[expected_start.value].value
        return Length(last - first)
    }

    // Fetches the length of the piece starting from the first line to 'index' or to the end of
    // the piece.
    fun accumulate_value_no_lf(buffers: BufferCollection, piece: Piece, index: Line): Length {
        val buffer = buffers.buffer_at(piece.index)
        val line_starts = buffer.line_starts
        // Extend it so we can capture the entire line content including newline.
        val expected_start = piece.first.line + index.value + 1
        val first = line_starts[piece.first.line.value].value + piece.first.column.value
        if (expected_start.value > piece.last.line.value) {
            val last = line_starts[piece.last.line.value].value + piece.last.column.value
            return if (last == first)
                Length(0)
            else if (buffer.buffer[last - 1] == '\n')
                Length(last - 1 - first)
            else
                Length(last - first)
        }
        val last = line_starts[expected_start.value].value
        return if (last == first)
            Length(0)
        else if (buffer.buffer[last - 1] == '\n')
            Length(last - 1 - first)
        else
            Length(last - first)
    }

//    private fun populate_from_node(buf: String, buffers: BufferCollection, node: RedBlackTree) {
//        TODO()
//    }
//
//    private fun populate_from_node(buf: String, buffers: BufferCollection, node: RedBlackTree, line_index: Line) {
//        TODO()
//    }

    private fun line_feed_count(buffers: BufferCollection, index: BufferIndex, start: BufferCursor, end: BufferCursor): LFCount {
        // If the end position is the beginning of a new line, then we can just return the difference in lines.
        if (end.column.value == 0)
            return LFCount(end.line.value - start.line.value)

        val starts = buffers.buffer_at(index).line_starts

        // It means, there is no LF after end.
        if (end.line == Line(starts.size - 1))
            return LFCount(end.line.value - start.line.value)

        // Due to the check above, we know that there's at least one more line after 'end.line'.
        val next_start_offset = starts[end.line.value + 1]
        val end_offset = starts[end.line.value].value + end.column.value

        // There are more than 1 character after end, which means it can't be LF.
        if (next_start_offset.value > end_offset + 1)
            return LFCount(end.line.value - start.line.value)

        // This must be the case.  next_start_offset is a line down, so it is
        // not possible for end_offset to be < it at this point.
        check(end_offset + 1 == next_start_offset.value)
        return LFCount(end.line.value - start.line.value)
    }

    fun node_at(buffers: BufferCollection, node: RedBlackTree, off: CharOffset): NodePosition {
        var node_start_offset = 0
        var newline_count = 0
        var local_node = node
        var local_off = off
        while (!local_node.isEmpty()) {
            if (local_node.root().left_subtree_length.value > local_off.value) {
                local_node = local_node.left()
            }
            else if (local_node.root().left_subtree_length.value + local_node.root().piece.length.value > local_off.value) {
                node_start_offset += local_node.root().left_subtree_length.value
                newline_count += local_node.root().left_subtree_lf_count.value
                // Now we find the line within this piece.
                val remainder = Length(local_off.value - local_node.root().left_subtree_length.value)
                val pos = buffer_position(buffers, node.root().piece, remainder)
                // Note: since buffer_position will return us a newline relative to the buffer itself, we need
                // to retract it by the starting line of the piece to get the real difference.
                newline_count += pos.line.value - local_node.root().piece.first.line.value
                return NodePosition(
                    node = local_node.root(),
                    remainder = remainder,
                    start_offset = CharOffset(node_start_offset),
                    line = Line(newline_count)
                )
            }
            else {
                // If there are no more nodes to traverse to, return this final node.
                if (local_node.right().isEmpty()) {
                    val offset_amount = local_node.root().left_subtree_length.value
                    node_start_offset += offset_amount
                    newline_count += local_node.root().left_subtree_lf_count.value + local_node.root().piece.newline_count.value
                    // Now we find the line within this piece.
                    val remainder = node.root().piece.length
                    return NodePosition(
                        node = local_node.root(),
                        remainder = remainder,
                        start_offset = CharOffset(node_start_offset),
                        line = Line(newline_count + 1)
                    )
                }
                val offset_amount = local_node.root().left_subtree_length.value + local_node.root().piece.length.value
                local_off = CharOffset(local_off.value - offset_amount)
                node_start_offset += offset_amount
                newline_count += local_node.root().left_subtree_lf_count.value + local_node.root().piece.newline_count.value
                local_node = local_node.right()
            }
        }
        return NodePosition()
    }

    private fun buffer_position(buffers: BufferCollection, piece: Piece, remainder: Length): BufferCursor {
        val starts = buffers.buffer_at(piece.index).line_starts
        val start_offset = starts[piece.first.line.value].value + piece.first.column.value
        val offset = start_offset + remainder.value

        // Binary search for 'offset' between start and ending offset.
        var low = piece.first.line.value
        var high = piece.last.line.value

        var mid = 0
        var mid_start = 0
        var mid_stop: Int

        while (low <= high) {
            mid = low + ((high - low) / 2)
            mid_start = starts[mid].value

            if (mid == high)
                break
            mid_stop = starts[mid + 1].value

            if (offset < mid_start)
                high = mid - 1
            else if (offset >= mid_stop)
                low = mid + 1;
            else
                break
        }

        return BufferCursor(
            line = Line(mid),
            column = Column(offset - mid_start)
        )
    }

    private fun char_at(buffers: BufferCollection, node: RedBlackTree, offset: CharOffset): Char? {
        val result = node_at(buffers, node, offset)
        if (result.node == null)
            return null
        val buffer = buffers.buffer_at(result.node.piece.index)
        val buf_offset = buffers.buffer_offset(result.node.piece.index, result.node.piece.first)
        val p: Char = buffer.buffer[buf_offset.value + result.remainder.value]
        return p
    }

    private fun trim_piece_right(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        val orig_end_offset = buffers.buffer_offset(piece.index, piece.last)

        val new_end_offset = buffers.buffer_offset(piece.index, pos)
        val new_lf_count = line_feed_count(buffers, piece.index, piece.first, pos)

        val len_delta = new_end_offset.distance(orig_end_offset)
        val new_len = piece.length - len_delta

        return piece.copy(
            last = pos,
            newline_count = new_lf_count,
            length = new_len
        )
    }

    private fun trim_piece_left(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        val orig_start_offset = buffers.buffer_offset(piece.index, piece.first)

        val new_start_offset = buffers.buffer_offset(piece.index, pos)
        val new_lf_count = line_feed_count(buffers, piece.index, pos, piece.last)

        val len_delta = orig_start_offset.distance(new_start_offset)
        val new_len = piece.length - len_delta

        return piece.copy(
            first = pos,
            newline_count = new_lf_count,
            length = new_len
        )
    }

    data class ShrinkResult(val left: Piece, val right: Piece)

    private fun shrink_piece(buffers: BufferCollection, piece: Piece, first: BufferCursor, last: BufferCursor): ShrinkResult {
        val left = trim_piece_right(buffers, piece, first)
        val right = trim_piece_left(buffers, piece, last)

        return ShrinkResult(
            left = left,
            right = right
        )
    }

    // Direct mutations.

    private fun assemble_line(node: RedBlackTree, line: Line): String {
        val sb = StringBuilder()
        val line_offset = CharOffset()
        line_start(::accumulate_value, line_offset, buffers, node, line))
        val walker = TreeWalker(this, line_offset)
        while (!walker.exhausted()) {
            val c = walker.next()
            if (c == '\n')
                break;
            sb.append(c)
        }
        return sb.toString()
    }

    private fun build_piece(txt: String): Piece {
        val start_offset = buffers.mod_buffer.buffer.length
        scratch_starts = populate_line_starts(txt)
        val start = last_insert
        // TODO: Handle CRLF (where the new buffer starts with LF and the end of our buffer ends with CR).
        // Offset the new starts relative to the existing buffer.
        scratch_starts.indices.forEach { i ->
            scratch_starts[i] = LineStart(scratch_starts[i].value + start_offset)
        }

        // Append new starts.
        // Note: we can drop the first start because the algorithm always adds an empty start.
        buffers.mod_buffer.line_starts.addAll(scratch_starts)

        buffers.mod_buffer.buffer += txt

        // Build the new piece for the inserted buffer.
        val end_offset = buffers.mod_buffer.buffer.length
        val end_index = buffers.mod_buffer.line_starts.size - 1
        val end_col = end_offset - buffers.mod_buffer.line_starts[end_index].value
        val end_pos = BufferCursor(line = Line(end_index), column = Column(end_col))
        val piece = Piece(
            index = BufferIndex.ModBuf,
            first = start,
            last = end_pos,
            length = Length(end_offset - start_offset),
            newline_count = line_feed_count(buffers, BufferIndex.ModBuf, start, end_pos)
        )
        // Update the last insertion.
        last_insert = end_pos
        return piece
    }

    private fun combine_pieces(existing: NodePosition, new_piece: Piece): Piece {
        // This transformation is only valid under the following conditions.
        val node = existing.node
        check(node != null)
        check(node.piece.index == BufferIndex.ModBuf)

        // This assumes that the piece was just built.
        check(node.piece.last == new_piece.first)

        val old_piece = node.piece
        val result = new_piece.copy(
            newline_count = new_piece.newline_count + old_piece.newline_count,
            length = new_piece.length + old_piece.length
        )
        root = root.remove(existing.start_offset)
            .insert(RedBlackTree.NodeData(new_piece), existing.start_offset)

        return result
    }

    private fun remove_node_range(first: NodePosition, length: Length) {
        var localLength = length
        var localFirst = first
        // Remove pieces until we reach the desired length.
        var deleted_len = Length()

        // Because we could be deleting content in the range starting at 'first' where the piece
        // length could be much larger than 'length', we need to adjust 'length' to contain the
        // delta in length within the piece to the end where 'length' starts:
        // "abcd"  "efg"
        //     ^     ^
        //     |_____|
        //      length to delete = 3
        // P1 length: 4
        // P2 length: 3 (though this length does not matter)
        // We're going to remove all of 'P1' and 'P2' in this range and the caller will re-insert
        // these pieces with the correct lengths.  If we fail to adjust 'length' we will delete P1
        // and believe that the entire range was deleted.
        val node = localFirst.node
        check(node != null)
        val total_length = node.piece.length
        // (total - remainder) is the section of 'length' where 'first' intersects.
        localLength = localLength - (total_length - localFirst.remainder) + total_length
        val delete_at_offset = localFirst.start_offset
        while (deleted_len.value < localLength.value && localFirst.node != null) {
            deleted_len += node.piece.length
            root = root.remove(delete_at_offset)
            localFirst = node_at(buffers, root, delete_at_offset)
        }
    }

    private fun compute_buffer_meta() {
        meta = root.compute_buffer_meta()
    }

    private fun append_undo(old_root: RedBlackTree, op_offset: CharOffset) {
        // Can't redo if we're creating a new undo entry.
        if (!redo_stack.isEmpty()) {
            redo_stack.clear()
        }
        undo_stack.add(0, UndoRedoEntry(root = old_root, op_offset = op_offset))
    }

    companion object {
        fun populate_line_starts(buf: String): LineStarts =
            buf.indices
                .filter { it == 0 || buf[it - 1] == '\n' }
                .map { LineStart(it) }
                .toMutableList()

        fun line_start(accumulate: Accumulator, offset: CharOffset, buffers: BufferCollection, node: RedBlackTree, line: Line): CharOffset {
            if (node.isEmpty()) return offset

            check(line != Line.IndexBeginning)
            var line_index = line.value - 1

            if (node.root().left_subtree_lf_count.value >= line_index) {
                return line_start(accumulate, offset, buffers, node.left(), line)
            }

            // The desired line is directly within the node.
            else if ((node.root().left_subtree_lf_count + node.root().piece.newline_count).value >= line_index) {
                line_index -= node.root().left_subtree_lf_count.value
                var len = node.root().left_subtree_length
                if (line_index != 0) {
                    len += accumulate(buffers, node.root().piece, Line(line_index - 1))
                }
                return offset + len
            }

            // assemble the LHS and RHS.
            else {
                // This case implies that 'left_subtree_lf_count' is strictly < line_index.
                // The content is somewhere in the middle.
                line_index -= (node.root().left_subtree_lf_count + node.root().piece.newline_count).value
                val offset2 = offset + node.root().left_subtree_length + node.root().piece.length
                return line_start(accumulate, offset2, buffers, node.right(), Line(line_index + 1))
            }
        }
    }
}




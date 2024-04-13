package com.dgmltn.ded.fredbuf.piecetree

import co.touchlab.kermit.Logger
import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Column
import com.dgmltn.ded.fredbuf.editor.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferCursor
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.LFCount
import com.dgmltn.ded.fredbuf.redblacktree.Line
import com.dgmltn.ded.fredbuf.redblacktree.Piece
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree.Companion.checkSatisfiesRbInvariants

class Tree(buffers: Buffers = emptyList()) {
    val buffers: BufferCollection = BufferCollection(buffers)
    var root: RedBlackTree = RedBlackTree()
    var meta: BufferMeta = BufferMeta()
    private var scratchStarts = mutableListOf<LineStart>()
    private var lastInsert: BufferCursor = BufferCursor()
    // Note: This is absolute position. Initialize to nonsense value.
    private var endLastInsert: CharOffset = CharOffset.Sentinel
    private val undoStack = mutableListOf<UndoRedoEntry>()
    private val redoStack = mutableListOf<UndoRedoEntry>()

    init {
        buildTree()
    }

    // Interface.
    // Initialization after populating initial immutable buffers from ctor.
    private fun buildTree() {
        buffers.modBuffer.lineStarts.clear()
        buffers.modBuffer.buffer = ""

        // In order to maintain the invariant of other buffers, the mod_buffer needs a single line-start of 0.
        buffers.modBuffer.lineStarts.add(LineStart(0))

        lastInsert = BufferCursor()

        val bufCount = buffers.origBuffers.size
        Logger.d { "build_tree: buf_count = $bufCount" }

        var offset = CharOffset()
        (0 until bufCount).forEach { i ->
            val buf = buffers.origBuffers[i]
            Logger.d { "build_tree: i = $i, line_starts = ${buf.lineStarts}" }
            check(buf.lineStarts.isNotEmpty())
            Logger.d { "build_tree: buf.buffer: ${buf.buffer}" }

            // If this immutable buffer is empty, we can avoid creating a piece for it altogether.
            if (buf.buffer.isNotEmpty()) {
                val lastLine = Line(buf.lineStarts.size - 1)
                Logger.d { "build_tree: last_line = $lastLine" }
                // Create a new node that spans this buffer and retains an index to it.
                // Insert the node into the balanced tree.
                val piece = Piece(
                    index = BufferIndex(i),
                    first = BufferCursor(line = Line(0), column = Column(0)),
                    last = BufferCursor(line = lastLine, column = Column(buf.buffer.length - buf.lineStarts[lastLine.value].value)),
                    length = Length(buf.buffer.length),
                    // Note: the number of newlines
                    newlineCount = LFCount(lastLine.value)
                )
                Logger.d { "build_tree: next piece = $piece" }
                root = root.insert(RedBlackTree.NodeData(piece = piece), offset)
                offset += piece.length
            }

        }

        computeBufferMeta()
    }

    fun insert(offset: CharOffset, txt: String, suppress_history: SuppressHistory = SuppressHistory.No) {
        if (txt.isEmpty())
            return

        // This allows us to undo blocks of code.
        if (suppress_history == SuppressHistory.No
            && (endLastInsert != offset || root.isEmpty())) {
            appendUndo(root, offset)
        }

        internalInsert(offset, txt)
    }

    fun remove(offset: CharOffset, count: Length, suppress_history: SuppressHistory = SuppressHistory.No) {
        // Rule out the obvious noop.
        if (count.value == 0 || root.rootPtr == null)
            return

        if (suppress_history == SuppressHistory.No) {
            appendUndo(root, offset)
        }

        internalRemove(offset, count)
    }

    fun tryUndo(opOffset: CharOffset): UndoRedoResult {
        if (undoStack.isEmpty())
            return UndoRedoResult(
                success = false,
                opOffset = CharOffset(0)
            )

        redoStack.add(0, UndoRedoEntry(root, opOffset))
        val (node, undoOffset) = undoStack.removeFirst()
        root = node
        computeBufferMeta()
        return UndoRedoResult(
            success = true,
            opOffset = undoOffset
        )
    }

    fun try_redo(op_offset: CharOffset): UndoRedoResult {
        if (redoStack.isEmpty())
            return UndoRedoResult(
                success = false,
                opOffset = CharOffset(0)
            )

        undoStack.add(0, UndoRedoEntry(root, op_offset))
        val (node, redoOffset) = redoStack.removeFirst()
        root = node
        computeBufferMeta()
        return UndoRedoResult(
            success = true,
            opOffset = redoOffset
        )
    }

    // Direct history manipulation.
    // This will commit the current node to the history.  The offset provided will be the undo point later.
    fun commitHead(offset: CharOffset) {
        appendUndo(root, offset)
    }

    fun head(): RedBlackTree = root

    // Snaps the tree back to the specified root.  This needs to be called with a root that is derived from
    // the set of buffers based on its creation.
    fun snapTo(newRoot: RedBlackTree) {
        root = newRoot
        computeBufferMeta()
    }

    // Queries.
    fun getLineContent(line: Line): String =
        if (line == Line.IndexBeginning) ""
        else assembleLine(root, line)

    fun getLineContentCrlf(line: Line): IncompleteCRLF {
        val sb = StringBuilder()
        if (line == Line.IndexBeginning)
            return IncompleteCRLF.No
        val node = root
        if (node.isEmpty())
            return IncompleteCRLF.No
        // Trying this new logic for now.
        val line_offset = CharOffset()
        lineStart(::accumulateValue, line_offset, buffers, node, line)
        return trimCrlf(sb, TreeWalker(this, line_offset))
    }

    fun at(offset: CharOffset): Char? = charAt(buffers, root, offset)

    fun lineAt(offset: CharOffset): Line =
        if (isEmpty()) Line.Beginning
        else nodeAt(buffers, root, offset).line

    fun getLineRange(line: Line): LineRange = LineRange(
        first = lineStart(::accumulateValue, CharOffset(0), buffers, root, line),
        last = lineStart(::accumulateValueNoLf, CharOffset(0), buffers, root, line + 1)
    )

    fun getLineRangeCrlf(line: Line): LineRange =
        LineRange(
            first = lineStart(::accumulateValue, CharOffset(0), buffers, root, line),
            last = lineEndCrlf(CharOffset(0), buffers, root, root, line + 1)
        )

    fun getLineRangeWithNewline(line: Line): LineRange =
        LineRange(
            first = lineStart(::accumulateValue, CharOffset(0), buffers, root, line),
            last = lineStart(::accumulateValue, CharOffset(0), buffers, root, line + 1)
        )

    fun length(): Length = meta.totalContentLength

    fun isEmpty(): Boolean = meta.totalContentLength.value == 0

    fun lineFeedCount(): LFCount = meta.lfCount

    fun lineCount(): Length = Length(lineFeedCount().value + 1)

    fun owningSnap(): OwningSnapshot = OwningSnapshot(this)

    fun refSnap(): ReferenceSnapshot = ReferenceSnapshot(this)

    // Privates
    private fun printPiece(piece: Piece, tree: Tree, level: Int) {
        TODO()
    }

    private fun printTree(root: RedBlackTree, tree: Tree, level: Int = 0, nodeOffset: Int = 0) {
        TODO()
    }

    private fun printTree(tree: Tree) {
        TODO()
    }

    fun printBuffer(): String {
        val sb = StringBuilder("--- Entire Buffer ---\n")
        val walker = TreeWalker(this)
        while (!walker.exhausted()) {
            sb.append(walker.next())
        }
        sb.append("\n")
        return sb.toString()
    }

    private fun internalInsert(offset: CharOffset, txt: String) {
        check(txt.isNotEmpty())
        endLastInsert = offset + Length(txt.length)
        Logger.d { "INS: insert ===============================" }
        Logger.d { "INS: inserting at ${offset.value}-${endLastInsert.value}: '$txt'" }
        computeBufferMeta()
        root.checkSatisfiesRbInvariants()

        if (root.isEmpty()) {
            val piece = buildPiece(txt)
            root = root.insert(RedBlackTree.NodeData(piece = piece), CharOffset(0))
            Logger.d { "INS: root is empty, inserting at 0" }
            return
        }

        var result = nodeAt(buffers, root, offset)

        // If the offset is beyond the buffer, just select the last node.
        if (result.node == null) {
            var off = CharOffset(0)
            if (meta.totalContentLength != Length()) {
                off = off + meta.totalContentLength - Length(1)
            }
            result = nodeAt(buffers, root, off)
        }
        Logger.d { "INS: node is $result" }

        // There are 3 cases:
        // 1. We are inserting at the beginning of an existing node.
        // 2. We are inserting at the end of an existing node.
        // 3. We are inserting in the middle of the node.
        val node = result.node
        val remainder = result.remainder
        var nodeStartOffset = result.startOffset
//        val line = result.line
        check(node != null)
        val insertPos = bufferPosition(buffers, node.piece, remainder)

        // Case #1.
        if (nodeStartOffset == offset) {
            Logger.d { "INS: case 1: inserting at the beginning of an existing node" }
            // There's a bonus case here.  If our last insertion point was the same as this piece's
            // last and it inserted into the mod buffer, then we can simply 'extend' this piece by
            // the following process:
            // 1. Fetch the previous node (if we can) and compare.
            // 2. Build the new piece.
            // 3. Remove the old piece.
            // 4. Extend the old piece's length to the length of the newly created piece.
            // 5. Re-insert the new piece.
            if (offset.value != 0) {
                val prevNodeResult = nodeAt(buffers, root, offset - 1)
                if (prevNodeResult.node?.piece?.index == BufferIndex.ModBuf
                    && prevNodeResult.node.piece.last == lastInsert) {
                    val newPiece = buildPiece(txt)
                    val combined = combinePieces(prevNodeResult, newPiece)
                    Logger.d { "INS: bonus case. $newPiece -> $combined" }
                    return
                 }
            }
            val piece = buildPiece(txt)
            root.insert(RedBlackTree.NodeData(piece = piece), offset)
            return
        }

        val insideNode = offset < nodeStartOffset + node.piece.length

        // Case #2.
        if (!insideNode) {
            Logger.d { "INS: case 2: inserting at the end of an existing node" }
            // There's a bonus case here.  If our last insertion point was the same as this piece's
            // last and it inserted into the mod buffer, then we can simply 'extend' this piece by
            // the following process:
            // 1. Build the new piece.
            // 2. Remove the old piece.
            // 3. Extend the old piece's length to the length of the newly created piece.
            // 4. Re-insert the new piece.
            if (node.piece.index == BufferIndex.ModBuf && node.piece.last == lastInsert) {
                val new_piece = buildPiece(txt)
                val combined = combinePieces(result, new_piece)
                Logger.d { "INS: bonus case. $new_piece -> $combined" }
                return
            }
            // Insert the new piece at the end.
            val piece = buildPiece(txt)
            Logger.d { "INS: piece = $piece" }
            root.insert(RedBlackTree.NodeData(piece = piece), offset)
            return
        }

        // Case #3.
        // The basic approach here is to split the existing node into two pieces
        // and insert the new piece in between them.
        Logger.d { "INS: case 3: inserting in the middle of an existing node" }
        val newLenRight = buffers.bufferOffset(node.piece.index, insertPos).distance(buffers.bufferOffset(node.piece.index, node.piece.last))
        val newPieceRight = node.piece.copy(
            first = insertPos,
            length = newLenRight,
            newlineCount = lineFeedCount(buffers, node.piece.index, insertPos, node.piece.last)
        )

        // Remove the original node tail.
        val newPieceLeft = trimPieceRight(buffers, node.piece, insertPos)

        val newPiece = buildPiece(txt)

        // Remove the original node.
        root = root.remove(nodeStartOffset)

        // Insert the left.
        root = root.insert(RedBlackTree.NodeData(piece = newPieceLeft), nodeStartOffset)
        Logger.d { "INS: left = $newPieceLeft" }

        // Insert the new mid.
        nodeStartOffset += newPieceLeft.length
        root = root.insert(RedBlackTree.NodeData(piece = newPiece), nodeStartOffset)
        Logger.d { "INS: mid = $newPiece" }

        // Insert remainder.
        nodeStartOffset += newPiece.length
        root = root.insert(RedBlackTree.NodeData(piece = newPieceRight), nodeStartOffset)
        Logger.d { "INS: right = $newPieceRight" }
    }

    private fun internalRemove(offset: CharOffset, count: Length) {
        check(count.value != 0 && !root.isEmpty())

        computeBufferMeta()
        checkSatisfiesRbInvariants(root)

        val first = nodeAt(buffers, root, offset)
        val last = nodeAt(buffers, root, offset + count)
        val firstNode = first.node
        val lastNode = last.node

        val startSplitPos = bufferPosition(buffers, firstNode!!.piece, first.remainder)

        // Simple case: the range of characters we want to delete are
        // held directly within this node.  Remove the node, resize it
        // then add it back.
        if (firstNode == lastNode) {
            val endSplitPos = bufferPosition(buffers, firstNode.piece, last.remainder)

            // We're going to shrink the node starting from the beginning.
            if (first.startOffset == offset) {

                // Delete the entire node.
                if (count == firstNode.piece.length) {
                    root = root.remove(first.startOffset)
                    return
                }

                // Shrink the node.
                val newPiece = trimPieceLeft(buffers, firstNode.piece, endSplitPos)

                // Remove the old one and update.
                root = root.remove(first.startOffset)
                    .insert(RedBlackTree.NodeData(newPiece), first.startOffset)
                return
            }

            // Trim the tail of this piece.
            if (first.startOffset + firstNode.piece.length == offset + count) {
                val newPiece = trimPieceRight(buffers, firstNode.piece, startSplitPos)

                // Remove the old one and update.
                root = root.remove(first.startOffset)
                    .insert(RedBlackTree.NodeData(newPiece), first.startOffset)
                return
            }

            // The removed buffer is somewhere in the middle.  Trim it in both directions.
            val (left, right) = shrinkPiece(buffers, firstNode.piece, startSplitPos, endSplitPos)
            root = root.remove(first.startOffset)
                // Note: We insert right first so that the 'left' will be inserted
                // to the right node's left.
                .insert(RedBlackTree.NodeData(right), first.startOffset)
                .insert(RedBlackTree.NodeData(left), first.startOffset)
            return
        }

        // Traverse nodes and delete all nodes within the offset range. First we will build the
        // partial pieces for the nodes that will eventually make up this range.
        // There are four cases here:
        // 1. The entire first node is deleted as well as all of the last node.
        // 2. Part of the first node is deleted and all of the last node.
        // 3. Part of the first node is deleted and part of the last node.
        // 4. The entire first node is deleted and part of the last node.

        val newFirst = trimPieceRight(buffers, firstNode.piece, startSplitPos)
        if (lastNode == null) {
            removeNodeRange(first, count)
        }
        else {
            val endSplitPos = bufferPosition(buffers, lastNode.piece, last.remainder)
            val newLast = trimPieceLeft(buffers, lastNode.piece, endSplitPos)
            removeNodeRange(first, count)

            // There's an edge case here where we delete all the nodes up to 'last' but
            // last itself remains untouched.  The test of 'remainder' in 'last' can identify
            // this scenario to avoid inserting a duplicate of 'last'.
            if (last.remainder.value != 0) {
                if (newLast.length.value != 0) {
                    root = root.insert(RedBlackTree.NodeData(newLast), first.startOffset)
                }
            }
        }

        if (newFirst.length.value != 0) {
            root = root.insert(RedBlackTree.NodeData(newFirst), first.startOffset)
        }
    }

    fun lineEndCrlf(offset: CharOffset, buffers: BufferCollection, root: RedBlackTree, node: RedBlackTree, line: Line): CharOffset {
        if (node.isEmpty())
            return offset

        check(line != Line.IndexBeginning)
        var lineIndex = line.value - 1
        if (node.root().leftSubtreeLfCount.value >= lineIndex) {
            return lineEndCrlf(offset, buffers, root, node.left(), line)
        }

        // The desired line is directly within the node.
        else if ((node.root().leftSubtreeLfCount + node.root().piece.newlineCount).value >= lineIndex) {
            lineIndex -= node.root().leftSubtreeLfCount.value
            var len = node.root().leftSubtreeLength
            if (lineIndex != 0) {
                len += accumulateValueNoLf(buffers, node.root().piece, Line(lineIndex - 1))
            }

            // If the length is anything but 0, we need to check if the last character was a carriage return.
            if (len != Length()) {
                val lastCharOffset = offset.value + len.value - 1
                if (charAt(buffers, root, CharOffset(lastCharOffset)) == '\r' && charAt(buffers, root, CharOffset(lastCharOffset + 1)) == '\n') {
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
            lineIndex -= node.root().leftSubtreeLfCount.value + piece.newlineCount.value
            val newOffset = offset + node.root().leftSubtreeLength + piece.length
            return lineEndCrlf(newOffset, buffers, root, node.right(), Line(lineIndex + 1))
        }
    }

    // Fetches the length of the piece starting from the first line to 'index' or to the end of
    // the piece.
    fun accumulateValue(buffers: BufferCollection, piece: Piece, index: Line): Length {
        val buffer = buffers.bufferAt(piece.index)
        val lineStarts = buffer.lineStarts
        // Extend it so we can capture the entire line content including newline.
        val expectedStart = piece.first.line + index.value + 1
        val first = lineStarts[piece.first.line.value].value + piece.first.column.value
        if (expectedStart.value > piece.last.line.value) {
            val last = lineStarts[piece.last.line.value].value + piece.last.column.value
            return Length(last - first)
        }
        val last = lineStarts[expectedStart.value].value
        return Length(last - first)
    }

    // Fetches the length of the piece starting from the first line to 'index' or to the end of
    // the piece.
    fun accumulateValueNoLf(buffers: BufferCollection, piece: Piece, index: Line): Length {
        val buffer = buffers.bufferAt(piece.index)
        val lineStarts = buffer.lineStarts
        // Extend it so we can capture the entire line content including newline.
        val expectedStart = piece.first.line + index.value + 1
        val first = lineStarts[piece.first.line.value].value + piece.first.column.value
        if (expectedStart.value > piece.last.line.value) {
            val last = lineStarts[piece.last.line.value].value + piece.last.column.value
            return if (last == first)
                Length(0)
            else if (buffer.buffer[last - 1] == '\n')
                Length(last - 1 - first)
            else
                Length(last - first)
        }
        val last = lineStarts[expectedStart.value].value
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

    private fun lineFeedCount(buffers: BufferCollection, index: BufferIndex, start: BufferCursor, end: BufferCursor): LFCount {
        // If the end position is the beginning of a new line, then we can just return the difference in lines.
        if (end.column.value == 0)
            return LFCount(end.line.value - start.line.value)

        val starts = buffers.bufferAt(index).lineStarts

        // It means, there is no LF after end.
        if (end.line == Line(starts.size - 1))
            return LFCount(end.line.value - start.line.value)

        // Due to the check above, we know that there's at least one more line after 'end.line'.
        val nextStartOffset = starts[end.line.value + 1]
        val endOffset = starts[end.line.value].value + end.column.value

        // There are more than 1 character after end, which means it can't be LF.
        if (nextStartOffset.value > endOffset + 1)
            return LFCount(end.line.value - start.line.value)

        // This must be the case.  next_start_offset is a line down, so it is
        // not possible for end_offset to be < it at this point.
        check(endOffset + 1 == nextStartOffset.value)
        return LFCount(end.line.value - start.line.value)
    }

    fun nodeAt(buffers: BufferCollection, node: RedBlackTree, off: CharOffset): NodePosition {
        var nodeStartOffset = 0
        var newlineCount = 0
        var localNode = node
        var localOff = off
        while (!localNode.isEmpty()) {
            if (localNode.root().leftSubtreeLength.value > localOff.value) {
                localNode = localNode.left()
            }
            else if (localNode.root().leftSubtreeLength.value + localNode.root().piece.length.value > localOff.value) {
                nodeStartOffset += localNode.root().leftSubtreeLength.value
                newlineCount += localNode.root().leftSubtreeLfCount.value
                // Now we find the line within this piece.
                val remainder = Length(localOff.value - localNode.root().leftSubtreeLength.value)
                val pos = bufferPosition(buffers, node.root().piece, remainder)
                // Note: since buffer_position will return us a newline relative to the buffer itself, we need
                // to retract it by the starting line of the piece to get the real difference.
                newlineCount += pos.line.value - localNode.root().piece.first.line.value
                return NodePosition(
                    node = localNode.root(),
                    remainder = remainder,
                    startOffset = CharOffset(nodeStartOffset),
                    line = Line(newlineCount)
                )
            }
            else {
                // If there are no more nodes to traverse to, return this final node.
                if (localNode.right().isEmpty()) {
                    val offsetAmount = localNode.root().leftSubtreeLength.value
                    nodeStartOffset += offsetAmount
                    newlineCount += localNode.root().leftSubtreeLfCount.value + localNode.root().piece.newlineCount.value
                    // Now we find the line within this piece.
                    val remainder = node.root().piece.length
                    return NodePosition(
                        node = localNode.root(),
                        remainder = remainder,
                        startOffset = CharOffset(nodeStartOffset),
                        line = Line(newlineCount + 1)
                    )
                }
                val offsetAmount = localNode.root().leftSubtreeLength.value + localNode.root().piece.length.value
                localOff = CharOffset(localOff.value - offsetAmount)
                nodeStartOffset += offsetAmount
                newlineCount += localNode.root().leftSubtreeLfCount.value + localNode.root().piece.newlineCount.value
                localNode = localNode.right()
            }
        }
        return NodePosition()
    }

    private fun bufferPosition(buffers: BufferCollection, piece: Piece, remainder: Length): BufferCursor {
        val starts = buffers.bufferAt(piece.index).lineStarts
        val startOffset = starts[piece.first.line.value].value + piece.first.column.value
        val offset = startOffset + remainder.value

        // Binary search for 'offset' between start and ending offset.
        var low = piece.first.line.value
        var high = piece.last.line.value

        var mid = 0
        var midStart = 0
        var midStop: Int

        while (low <= high) {
            mid = low + ((high - low) / 2)
            midStart = starts[mid].value

            if (mid == high)
                break
            midStop = starts[mid + 1].value

            if (offset < midStart)
                high = mid - 1
            else if (offset >= midStop)
                low = mid + 1;
            else
                break
        }

        return BufferCursor(
            line = Line(mid),
            column = Column(offset - midStart)
        )
    }

    private fun charAt(buffers: BufferCollection, node: RedBlackTree, offset: CharOffset): Char? {
        val result = nodeAt(buffers, node, offset)
        if (result.node == null)
            return null
        val buffer = buffers.bufferAt(result.node.piece.index)
        val bufOffset = buffers.bufferOffset(result.node.piece.index, result.node.piece.first)
        return buffer.buffer[bufOffset.value + result.remainder.value]
    }

    private fun trimPieceRight(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        val origEndOffset = buffers.bufferOffset(piece.index, piece.last)

        val newEndOffset = buffers.bufferOffset(piece.index, pos)
        val newLfCount = lineFeedCount(buffers, piece.index, piece.first, pos)

        val lenDelta = newEndOffset.distance(origEndOffset)
        val newLen = piece.length - lenDelta

        return piece.copy(
            last = pos,
            newlineCount = newLfCount,
            length = newLen
        )
    }

    private fun trimPieceLeft(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        val origStartOffset = buffers.bufferOffset(piece.index, piece.first)

        val newStartOffset = buffers.bufferOffset(piece.index, pos)
        val newLfCount = lineFeedCount(buffers, piece.index, pos, piece.last)

        val lenDelta = origStartOffset.distance(newStartOffset)
        val newLen = piece.length - lenDelta

        return piece.copy(
            first = pos,
            newlineCount = newLfCount,
            length = newLen
        )
    }

    data class ShrinkResult(val left: Piece, val right: Piece)

    private fun shrinkPiece(buffers: BufferCollection, piece: Piece, first: BufferCursor, last: BufferCursor): ShrinkResult {
        val left = trimPieceRight(buffers, piece, first)
        val right = trimPieceLeft(buffers, piece, last)

        return ShrinkResult(
            left = left,
            right = right
        )
    }

    // Direct mutations.

    private fun assembleLine(node: RedBlackTree, line: Line): String {
        val sb = StringBuilder()
        val offset = lineStart(::accumulateValue, CharOffset(), buffers, node, line)
        Logger.d { "assemble_line($line): starts at $offset" }
        val walker = TreeWalker(this, offset)
        while (!walker.exhausted()) {
            val c = walker.next()
            if (c == '\n')
                break
            sb.append(c)
        }
        return sb.toString()
            .also {
                Logger.d { "assemble_line($line): $it" }
            }
    }

    private fun buildPiece(txt: String): Piece {
        val startOffset = buffers.modBuffer.buffer.length
        scratchStarts = populateLineStarts(txt)
        val start = lastInsert
        // TODO: Handle CRLF (where the new buffer starts with LF and the end of our buffer ends with CR).
        // Offset the new starts relative to the existing buffer.
        scratchStarts.indices.forEach { i ->
            scratchStarts[i] = LineStart(scratchStarts[i].value + startOffset)
        }

        // Append new starts.
        // Note: we can drop the first start because the algorithm always adds an empty start.
        buffers.modBuffer.lineStarts.addAll(scratchStarts)

        buffers.modBuffer.buffer += txt

        // Build the new piece for the inserted buffer.
        val endOffset = buffers.modBuffer.buffer.length
        val endIndex = buffers.modBuffer.lineStarts.size - 1
        val endCol = endOffset - buffers.modBuffer.lineStarts[endIndex].value
        val endPos = BufferCursor(line = Line(endIndex), column = Column(endCol))
        val piece = Piece(
            index = BufferIndex.ModBuf,
            first = start,
            last = endPos,
            length = Length(endOffset - startOffset),
            newlineCount = lineFeedCount(buffers, BufferIndex.ModBuf, start, endPos)
        )
        // Update the last insertion.
        lastInsert = endPos
        return piece
    }

    private fun combinePieces(existing: NodePosition, new_piece: Piece): Piece {
        // This transformation is only valid under the following conditions.
        val node = existing.node
        check(node != null)
        check(node.piece.index == BufferIndex.ModBuf)

        // This assumes that the piece was just built.
        check(node.piece.last == new_piece.first)

        val old_piece = node.piece
        val result = new_piece.copy(
            newlineCount = new_piece.newlineCount + old_piece.newlineCount,
            length = new_piece.length + old_piece.length
        )
        root = root.remove(existing.startOffset)
            .insert(RedBlackTree.NodeData(new_piece), existing.startOffset)

        return result
    }

    private fun removeNodeRange(first: NodePosition, length: Length) {
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
        val totalLength = node.piece.length
        // (total - remainder) is the section of 'length' where 'first' intersects.
        localLength = localLength - (totalLength - localFirst.remainder) + totalLength
        val deleteAtOffset = localFirst.startOffset
        while (deleted_len.value < localLength.value && localFirst.node != null) {
            deleted_len += node.piece.length
            root = root.remove(deleteAtOffset)
            localFirst = nodeAt(buffers, root, deleteAtOffset)
        }
    }

    private fun computeBufferMeta() {
        meta = root.computeBufferMeta()
    }

    private fun appendUndo(oldRoot: RedBlackTree, opOffset: CharOffset) {
        // Can't redo if we're creating a new undo entry.
        if (redoStack.isNotEmpty()) {
            redoStack.clear()
        }
        undoStack.add(0, UndoRedoEntry(root = oldRoot, opOffset = opOffset))
    }

    companion object {
        fun populateLineStarts(buf: String): LineStarts =
            buf.indices
                .filter { it == 0 || buf[it - 1] == '\n' }
                .map { LineStart(it) }
                .toMutableList()

        fun lineStart(accumulate: Accumulator, offset: CharOffset, buffers: BufferCollection, node: RedBlackTree, line: Line): CharOffset {
            if (node.isEmpty()) return offset

            check(line != Line.IndexBeginning)
            var lineIndex = line.value - 1

            Logger.d { "line_start: line_index = $lineIndex" }

            if (node.root().leftSubtreeLfCount.value >= lineIndex) {
                Logger.d { "line_start: in the left node" }
                return lineStart(accumulate, offset, buffers, node.left(), line)
            }

            // The desired line is directly within the node.
            else if ((node.root().leftSubtreeLfCount + node.root().piece.newlineCount).value >= lineIndex) {
                lineIndex -= node.root().leftSubtreeLfCount.value
                var len = node.root().leftSubtreeLength
                Logger.d { "line_start: the desired line is directly within the node (line_index = $lineIndex) (len = $len)" }
                if (lineIndex != 0) {
                    len += accumulate(buffers, node.root().piece, Line(lineIndex - 1))
                }
                Logger.d { "line_start returning ${offset + len}" }
                return offset + len
            }

            // assemble the LHS and RHS.
            else {
                // This case implies that 'left_subtree_lf_count' is strictly < line_index.
                // The content is somewhere in the middle.
                lineIndex -= (node.root().leftSubtreeLfCount + node.root().piece.newlineCount).value
                val offset2 = offset + node.root().leftSubtreeLength + node.root().piece.length
                return lineStart(accumulate, offset2, buffers, node.right(), Line(lineIndex + 1))
            }
        }
    }
}




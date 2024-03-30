package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.CharOffset
import com.dgmltn.ded.fredbuf.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree

class ReverseTreeWalker {

    private var buffers: BufferCollection
    private var root: RedBlackTree
    private var meta: BufferMeta
    private var stack = mutableListOf<StackEntry>()
    private var total_offset = CharOffset(0)
    private var first_ptr: BufferPointer? = null
    private var last_ptr: BufferPointer? = null

    constructor(tree: Tree, offset: CharOffset = CharOffset(0)) {
        buffers = tree.buffers
        root = tree.root
        meta = tree.meta
        stack.add(StackEntry(root))
        total_offset = offset
        fast_forward_to(offset)
    }
    constructor(snap: OwningSnapshot, offset: CharOffset = CharOffset(0)) {
        buffers = snap.buffers
        root = snap.root
        meta = snap.meta
        stack.add(StackEntry(root))
        total_offset = offset
        fast_forward_to(offset)
    }
    constructor(snap: ReferenceSnapshot, offset: CharOffset = CharOffset(0)) {
        buffers = snap.buffers
        root = snap.root
        meta = snap.meta
        stack.add(StackEntry(root))
        total_offset = offset
        fast_forward_to(offset)
    }

    inner class BufferPointer(val index: BufferIndex, val offset: CharOffset) {
        fun char(): Char = buffers.buffer_at(index).buffer[offset.value]
        operator fun minus(other: Int) = BufferPointer(index, offset - other)
    }

    fun current(): Char? {
        if (first_ptr == last_ptr) {
            populate_ptrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
        }
        return first_ptr?.minus(1)?.char()
    }

    fun next(): Char? {
        if (first_ptr == last_ptr) {
            populate_ptrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
            // Catchall.
            if (first_ptr == last_ptr)
                return next()
        }

        // Since CharOffset is unsigned, this will end up wrapping, both 'exhausted' and
        // 'remaining' will return 'true' and '0' respectively.
        total_offset -= 1

        // A dereference is the pointer value _before_ this actual pointer, just like
        // STL reverse iterator models.
        first_ptr = first_ptr?.minus(1)
        return first_ptr?.char()
    }

    fun seek(offset: CharOffset) {
        stack.clear()
        stack.add(StackEntry(node = root))
        total_offset = offset;
        fast_forward_to(offset)
    }

    fun exhausted(): Boolean {
        if (stack.isEmpty())
            return true

        // If we have not exhausted the pointers, we're still active.
        if (first_ptr != last_ptr)
            return false

        // If there's more than one entry on the stack, we're still active.
        if (stack.size > 1)
            return false

        // Now, if there's exactly one entry and that entry itself is exhausted (no right subtree)
        // we're done.
        val entry = stack.last()

        // We descended into a null child, we're done.
        if (entry.node.isEmpty())
            return true

        // Do we need this check for reverse iterators?
        if (entry.direction == Direction.Left && entry.node.left().isEmpty())
            return true

        return false
    }

    fun remaining(): Length {
        return CharOffset().distance(total_offset + 1)
    }

    fun offset(): CharOffset = total_offset

    // For Iterator-like behavior.
//    operator fun inc() {
//        return *this;
//    }

//    char operator*() {
//        return next();
//    }

    private fun populate_ptrs() {
        if (exhausted())
            return

        if (stack.last().node.isEmpty()) {
            stack.removeLast()
            populate_ptrs()
            return
        }

        var (node, dir) = stack.last()
        if (dir == Direction.Right) {
            if (!node.right().isEmpty()) {
                val right = node.right()
                // Change the dir for when we pop back.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                stack.add(StackEntry(right))
                populate_ptrs()
                return
            }

            // Otherwise, let's visit the center, we can actually fallthrough.
            stack.add(stack.removeLast().copy(direction = Direction.Center))
            dir = Direction.Center
        }

        if (dir == Direction.Center) {
            val piece = node.root().piece

            val first_offset = buffers.buffer_offset(piece.index, piece.first)
            val last_offset = buffers.buffer_offset(piece.index, piece.last)

            last_ptr = BufferPointer(piece.index, first_offset)
            first_ptr = BufferPointer(piece.index, last_offset)

            // Change this direction.
            stack.add(stack.removeLast().copy(direction = Direction.Left))
            return
        }

        check(dir == Direction.Left)
        val left = node.left()
        stack.removeLast()
        stack.add(StackEntry(left))
        populate_ptrs()
    }

    private fun fast_forward_to(offset: CharOffset) {
        var node = root
        var relOffset = offset
        while (!node.isEmpty()) {
            if (node.root().left_subtree_length.value > relOffset.value) {
                check(stack.isNotEmpty())

                // This parent is no longer relevant.
                stack.removeLast()
                node = node.left()
                stack.add(StackEntry(node))
            }

            // It is inside this node.
            else if (node.root().left_subtree_length.value + node.root().piece.length.value > relOffset.value) {
                stack.add(stack.removeLast().copy(direction = Direction.Left))

                // Make the offset relative to this piece.
                relOffset -= node.root().left_subtree_length.value
                val piece = node.root().piece
                val first_offset = buffers.buffer_offset(piece.index, piece.first)
                last_ptr = BufferPointer(piece.index, first_offset)

                // We extend offset because it is the point where we want to start and because this walker works by dereferencing
                // 'first_ptr - 1', offset + 1 is our 'begin'.
                first_ptr = BufferPointer(piece.index, first_offset + relOffset + 1)
                return
            }

            else {
                // For when we revisit this node.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                val offset_amount = node.root().left_subtree_length.value + node.root().piece.length.value
                relOffset -= offset_amount
                node = node.right()
                stack.add(StackEntry(node))
            }
        }
    }

    enum class Direction {
        Left, Center, Right
    }

    data class StackEntry(
        val node: RedBlackTree,
        val direction: Direction = Direction.Right
    )

    data object WalkSentinel

//    inline TreeWalker begin(const Tree& tree) {
//        return TreeWalker{ &tree };
//    }
//
//    constexpr WalkSentinel end(const Tree&) {
//        return WalkSentinel{ };
//    }
//
//    inline bool operator==(const TreeWalker& walker, WalkSentinel) {
//        return walker.exhausted();
//    }

    enum class EmptySelection {
        No, Yes
    }

    data class SelectionMeta(
        val snap: OwningSnapshot,
        val first: CharOffset,
        val last: CharOffset,
        val empty: EmptySelection
    )
}
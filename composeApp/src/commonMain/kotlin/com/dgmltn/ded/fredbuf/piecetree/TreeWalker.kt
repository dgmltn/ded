package com.dgmltn.ded.fredbuf.piecetree

import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Length
import com.dgmltn.ded.fredbuf.redblacktree.BufferIndex
import com.dgmltn.ded.fredbuf.redblacktree.RedBlackTree

interface ITreeWalker {
    fun current(): Char?
    fun next(): Char?
    fun seek(offset: CharOffset)
    fun exhausted(): Boolean
    fun remaining(): Length
    fun offset(): CharOffset
}

class TreeWalker: ITreeWalker {
    private var buffers: BufferCollection
    private var root: RedBlackTree
    private var meta: BufferMeta
    private var stack = mutableListOf<StackEntry>()
    private var totalOffset: CharOffset = CharOffset(0)
    private var firstPtr: BufferPointer? = null
    private var lastPtr: BufferPointer? = null

    constructor(tree: Tree, offset: CharOffset = CharOffset(0)) {
        buffers = tree.buffers
        root = tree.root
        meta = tree.meta
        stack.add(StackEntry(root))
        totalOffset = offset
        fastForwardTo(offset)
    }
    constructor(snap: ISnapshot, offset: CharOffset = CharOffset(0)) {
        buffers = snap.buffers
        root = snap.root
        meta = snap.meta
        stack.add(StackEntry(root))
        totalOffset = offset
        fastForwardTo(offset)
    }

    inner class BufferPointer(val index: BufferIndex, val offset: CharOffset) {
        fun char(): Char? = buffers.bufferAt(index).buffer.let { if (offset.value >= it.length) null else it[offset.value] }
        operator fun minus(other: Int) = BufferPointer(index, offset - other)
        operator fun plus(other: Int) = BufferPointer(index, offset + other)

        override fun equals(other: Any?): Boolean =
            other is BufferPointer && index.value == other.index.value && offset.value == other.offset.value

        override fun toString(): String = "*[$index,$offset] = ${char()}"
    }

    override fun current(): Char? {
        if (firstPtr == lastPtr) {
            populatePtrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
        }
        return firstPtr?.char()
    }

    override fun next(): Char? {
        if (firstPtr == lastPtr) {
            populatePtrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
            // Catchall.
            if (firstPtr == lastPtr)
                return next()
        }
        totalOffset += 1
        val ret = firstPtr?.char()
        firstPtr = firstPtr?.plus(1)
        return ret
    }

    override fun seek(offset: CharOffset) {
        stack.clear()
        stack.add(StackEntry(root))
        totalOffset = offset
        fastForwardTo(offset)
    }

    override fun exhausted(): Boolean {
        if (stack.isEmpty())
            return true

        // If we have not exhausted the pointers, we're still active.
        if (firstPtr != lastPtr)
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

        if (entry.direction == Direction.Right && entry.node.right().isEmpty())
            return true

        return false
    }

    override fun remaining(): Length =
        meta.totalContentLength - CharOffset().distance(totalOffset)

    override fun offset(): CharOffset = totalOffset

    // For Iterator-like behavior.
//    operator fun inc() {
//        return *this;
//    }

//    char operator*() {
//        return next();
//    }

    private fun populatePtrs() {
        if (exhausted())
            return

        if (stack.last().node.isEmpty()) {
            stack.removeLast()
            populatePtrs()
            return
        }

        var (node, dir) = stack.last()
        if (dir == Direction.Left) {
            if (!node.left().isEmpty()) {
                val left = node.left()
                // Change the dir for when we pop back.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                stack.add(StackEntry(left))
                populatePtrs()
                return
            }
            // Otherwise, let's visit the center, we can actually fallthrough.
            stack.add(stack.removeLast().copy(direction = Direction.Center))
            dir = Direction.Center
        }

        if (dir == Direction.Center) {
            val piece = node.root().piece

            val firstOffset = buffers.bufferOffset(piece.index, piece.first)
            val lastOffset = buffers.bufferOffset(piece.index, piece.last)

            firstPtr = BufferPointer(piece.index, firstOffset)
            lastPtr = BufferPointer(piece.index, lastOffset)
            // Change this direction.
            stack.add(stack.removeLast().copy(direction = Direction.Right))
            return
        }

        check(dir == Direction.Right)
        val right = node.right()
        stack.removeLast()
        stack.add(StackEntry(right))
        populatePtrs()
    }

    private fun fastForwardTo(offset: CharOffset) {
        var node = root
        var relOffset = offset
        while (!node.isEmpty()) {
            if (node.root().leftSubtreeLength.value > relOffset.value) {
                // For when we revisit this node.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                node = node.left()
                stack.add(StackEntry(node))
            }

            // It is inside this node.
            else if ((node.root().leftSubtreeLength + node.root().piece.length).value > relOffset.value) {
                stack.add(stack.removeLast().copy(direction = Direction.Right))

                // Make the offset relative to this piece.
                relOffset -= node.root().leftSubtreeLength.value
                val piece = node.root().piece
                val firstOffset = buffers.bufferOffset(piece.index, piece.first)
                val lastOffset = buffers.bufferOffset(piece.index, piece.last)

                firstPtr = BufferPointer(piece.index, firstOffset + relOffset)
                lastPtr = BufferPointer(piece.index, lastOffset)

                return
            }
            else {
                check(stack.isNotEmpty())

                // This parent is no longer relevant.
                stack.removeLast()
                val offsetAmount: Int = node.root().leftSubtreeLength.value + node.root().piece.length.value
                relOffset -= offsetAmount
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
        val direction: Direction = Direction.Left
    )
}

class ReverseTreeWalker: ITreeWalker {

    private var buffers: BufferCollection
    private var root: RedBlackTree
    private var meta: BufferMeta
    private var stack = mutableListOf<StackEntry>()
    private var totalOffset = CharOffset(0)
    private var firstPtr: BufferPointer? = null
    private var lastPtr: BufferPointer? = null

    constructor(tree: Tree, offset: CharOffset = CharOffset(0)) {
        buffers = tree.buffers
        root = tree.root
        meta = tree.meta
        stack.add(StackEntry(root))
        totalOffset = offset
        fastForwardTo(offset)
    }
    constructor(snap: ISnapshot, offset: CharOffset = CharOffset(0)) {
        buffers = snap.buffers
        root = snap.root
        meta = snap.meta
        stack.add(StackEntry(root))
        totalOffset = offset
        fastForwardTo(offset)
    }

    inner class BufferPointer(val index: BufferIndex, val offset: CharOffset) {
        fun char(): Char = buffers.bufferAt(index).buffer[offset.value]
        operator fun minus(other: Int) = BufferPointer(index, offset - other)
    }

    override fun current(): Char? {
        if (firstPtr == lastPtr) {
            populatePtrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
        }
        return firstPtr?.minus(1)?.char()
    }

    override fun next(): Char? {
        if (firstPtr == lastPtr) {
            populatePtrs()
            // If this is exhausted, we're done.
            if (exhausted())
                return null
            // Catchall.
            if (firstPtr == lastPtr)
                return next()
        }

        // Since CharOffset is unsigned, this will end up wrapping, both 'exhausted' and
        // 'remaining' will return 'true' and '0' respectively.
        totalOffset -= 1

        // A dereference is the pointer value _before_ this actual pointer, just like
        // STL reverse iterator models.
        firstPtr = firstPtr?.minus(1)
        return firstPtr?.char()
    }

    override fun seek(offset: CharOffset) {
        stack.clear()
        stack.add(StackEntry(node = root))
        totalOffset = offset;
        fastForwardTo(offset)
    }

    override fun exhausted(): Boolean {
        if (stack.isEmpty())
            return true

        // If we have not exhausted the pointers, we're still active.
        if (firstPtr != lastPtr)
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

    override fun remaining(): Length {
        return CharOffset().distance(totalOffset + 1)
    }

    override fun offset(): CharOffset = totalOffset

    // For Iterator-like behavior.
//    operator fun inc() {
//        return *this;
//    }

//    char operator*() {
//        return next();
//    }

    private fun populatePtrs() {
        if (exhausted())
            return

        if (stack.last().node.isEmpty()) {
            stack.removeLast()
            populatePtrs()
            return
        }

        var (node, dir) = stack.last()
        if (dir == Direction.Right) {
            if (!node.right().isEmpty()) {
                val right = node.right()
                // Change the dir for when we pop back.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                stack.add(StackEntry(right))
                populatePtrs()
                return
            }

            // Otherwise, let's visit the center, we can actually fallthrough.
            stack.add(stack.removeLast().copy(direction = Direction.Center))
            dir = Direction.Center
        }

        if (dir == Direction.Center) {
            val piece = node.root().piece

            val first_offset = buffers.bufferOffset(piece.index, piece.first)
            val last_offset = buffers.bufferOffset(piece.index, piece.last)

            lastPtr = BufferPointer(piece.index, first_offset)
            firstPtr = BufferPointer(piece.index, last_offset)

            // Change this direction.
            stack.add(stack.removeLast().copy(direction = Direction.Left))
            return
        }

        check(dir == Direction.Left)
        val left = node.left()
        stack.removeLast()
        stack.add(StackEntry(left))
        populatePtrs()
    }

    private fun fastForwardTo(offset: CharOffset) {
        var node = root
        var relOffset = offset
        while (!node.isEmpty()) {
            if (node.root().leftSubtreeLength.value > relOffset.value) {
                check(stack.isNotEmpty())

                // This parent is no longer relevant.
                stack.removeLast()
                node = node.left()
                stack.add(StackEntry(node))
            }

            // It is inside this node.
            else if (node.root().leftSubtreeLength.value + node.root().piece.length.value > relOffset.value) {
                stack.add(stack.removeLast().copy(direction = Direction.Left))

                // Make the offset relative to this piece.
                relOffset -= node.root().leftSubtreeLength.value
                val piece = node.root().piece
                val first_offset = buffers.bufferOffset(piece.index, piece.first)
                lastPtr = BufferPointer(piece.index, first_offset)

                // We extend offset because it is the point where we want to start and because this walker works by dereferencing
                // 'first_ptr - 1', offset + 1 is our 'begin'.
                firstPtr = BufferPointer(piece.index, first_offset + relOffset + 1)
                return
            }

            else {
                // For when we revisit this node.
                stack.add(stack.removeLast().copy(direction = Direction.Center))
                val offset_amount = node.root().leftSubtreeLength.value + node.root().piece.length.value
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

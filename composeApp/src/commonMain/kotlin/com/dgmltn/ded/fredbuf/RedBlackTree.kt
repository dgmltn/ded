package com.dgmltn.ded.fredbuf

//TODO: make this generic, and replace NodeData with <T>?

data class BufferIndex(val value: Int) {
    companion object {
        val ModBuf = BufferIndex(-1)
    }
}

data class Line(val value: Int) {
    companion object {
        val IndexBeginning = Line(-1)
        val Beginning = Line(-2)
    }
}

data class LFCount(val value: Int) {
    operator fun plus(other: LFCount) = LFCount(this.value + other.value)
}

data class BufferCursor(
    val line: Line, // Relative line in the current buffer.
    val column: Column // Column into the current line.
)

data class Piece(
    val index: BufferIndex, // Index into a buffer in PieceTree.  This could be an immutable buffer or the mutable buffer.
    val first: BufferCursor,
    val last: BufferCursor,
    val length: Length,
    val newline_count: LFCount
)

class RedBlackTree(val root_ptr: Node? = null) {

    constructor(c: Color, lft: RedBlackTree, data: NodeData, rgt: RedBlackTree): this(Node(c, lft.root_ptr, attribute(data, lft), rgt.root_ptr))

    enum class Color {
        Red, Black, DoubleBlack;
        override fun toString() = name
    }

    data class NodeData(
        val piece: Piece,
        val left_subtree_length: Length,
        val left_subtree_lf_count: LFCount
    )

    data class Node(val color: Color, val left: Node?, val data: NodeData, val right: Node?)

    data class ColorTree(val color: Color, val tree: RedBlackTree?) {
        private constructor(): this(Color.DoubleBlack, null)
        constructor(tree: RedBlackTree): this(if (tree.isEmpty()) Color.Black else tree.root_color(), tree)
        constructor(c: Color, lft: RedBlackTree, x: NodeData, rgt: RedBlackTree): this(c, RedBlackTree(c, lft, x, rgt))

        companion object {
            fun double_black() = ColorTree()
        }
    }

    data class WalkResult(
        val tree: RedBlackTree,
        val accumulated_offset: CharOffset
    )

    fun isEmpty() = root_ptr == null

    fun root(): NodeData {
        check(!isEmpty()) { "Tree is empty!" }
        return root_ptr!!.data
    }

    fun left(): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(root_ptr!!.left)
    }

    fun right(): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(root_ptr!!.right)
    }

    fun root_color(): Color {
        check(!isEmpty()) { "Tree is empty!" }
        return root_ptr!!.color
    }

    fun insert(x: NodeData, at: CharOffset): RedBlackTree {
        val t = ins(x, at, CharOffset(0))
        return RedBlackTree(Color.Black, t.left(), t.root(), t.right())
    }

    fun ins(x: NodeData, at: CharOffset, total_offset: CharOffset): RedBlackTree {
        if (isEmpty())
            return RedBlackTree(Color.Red, RedBlackTree(), x, RedBlackTree())

        val y = root()
        if (at < total_offset + y.left_subtree_length + y.piece.length)
            return balance(root_color(), left().ins(x, at, total_offset), y, right())

        return balance(root_color(), left(), y, right().ins(x, at, total_offset + y.left_subtree_length + y.piece.length))
    }

    fun doubled_left(): Boolean =
        !isEmpty()
                && root_color() == Color.Red
                && !left().isEmpty()
                && left().root_color() == Color.Red

    fun doubled_right(): Boolean =
        !isEmpty()
                && root_color() == Color.Red
                && !right().isEmpty()
                && right().root_color() != Color.Red

    fun paint(c: Color): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(c, left(), root(), right())
    }

    fun tree_length(): Length =
        if (isEmpty()) Length(0) else root().left_subtree_length + root().piece.length + right().tree_length()

    fun tree_lf_count(): LFCount =
        if (isEmpty()) LFCount(0) else root().left_subtree_lf_count + root().piece.newline_count + right().tree_lf_count()

    fun remove(at: CharOffset): RedBlackTree {
        val t = rem(this, at, CharOffset(0))
        if (t.isEmpty())
            return RedBlackTree()

        return RedBlackTree(
            c = Color.Black,
            lft = t.left(),
            data = t.root(),
            rgt = t.right()
        )
    }

    companion object {
        fun attribute(data: NodeData, left: RedBlackTree): NodeData =
            data.copy(
                left_subtree_length = left.tree_length(),
                left_subtree_lf_count = left.tree_lf_count()
            )

        fun balance(node: RedBlackTree): RedBlackTree {
            // Two red children.
            if (
                !node.left().isEmpty() && node.left().root_color() == Color.Red
                && !node.right().isEmpty() && node.right().root_color() == Color.Red
            ) {
                val l = node.left().paint(Color.Black)
                val r = node.right().paint(Color.Black)
                return RedBlackTree(Color.Red, l, node.root(), r)
            }

            check(node.root_color() == Color.Black)

            return balance(node.root_color(), node.left(), node.root(), node.right())
        }

        fun balance(c: Color, lft: RedBlackTree, x: NodeData, rgt: RedBlackTree): RedBlackTree =
            if (c == Color.Black && lft.doubled_left())
                RedBlackTree(
                    c = Color.Red,
                    lft = lft.left().paint(Color.Black),
                    data = lft.root(),
                    rgt = RedBlackTree(Color.Black, lft.right(), x, rgt)
                )
            else if (c == Color.Black && lft.doubled_right())
                RedBlackTree(
                    c = Color.Red,
                    lft = RedBlackTree(Color.Black, lft.left(), lft.root(), lft.right().left()),
                    data = lft.right().root(),
                    rgt = RedBlackTree(Color.Black, lft.right().right(), x, rgt)
                )
            else if (c == Color.Black && rgt.doubled_left())
                RedBlackTree(
                    c = Color.Red,
                    lft = RedBlackTree(Color.Black, lft, x, rgt.left().left()),
                    data = rgt.left().root(),
                    rgt = RedBlackTree(Color.Black, rgt.left().right(), rgt.root(), rgt.right())
                )
            else if (c == Color.Black && rgt.doubled_right())
                RedBlackTree(
                    c = Color.Red,
                    lft = RedBlackTree(Color.Black, lft, x, rgt.left()),
                    data = rgt.root(),
                    rgt = rgt.right().paint(Color.Black)
                )
            else RedBlackTree(c, lft, x, rgt)

        fun pred(root: RedBlackTree, start_offset: CharOffset): WalkResult {
            var offset = start_offset
            var t = root.left()
            while (!t.right().isEmpty()) {
                offset = offset + t.root().left_subtree_length + t.root().piece.length
                t = t.right()
            }
            // Add the final offset from the last right node.
            offset += t.root().left_subtree_length
            return WalkResult(tree = t, accumulated_offset = offset)
        }

        fun remove_left(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            val new_left = rem(root.left(), at, total)
            val new_node = RedBlackTree(Color.Red, new_left, root.root(), root.right())

            // In this case, the root was a red node and must've had at least two children.
            if (!root.left().isEmpty() && root.left().root_color() == Color.Black)
                return balance_left(new_node)

            return new_node
        }

        fun remove_right(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            val y = root.root()
            val new_right = rem(root.right(), at, total + y.left_subtree_length + y.piece.length)
            val new_node = RedBlackTree(Color.Red, root.left(), root.root(), new_right)

            // In this case, the root was a red node and must've had at least two children.
            if (!root.right().isEmpty() && root.right().root_color() == Color.Black)
                return balance_right(new_node)

            return new_node
        }

        fun rem(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            if (root.isEmpty())
                return RedBlackTree()

            val y = root.root()
            if (at < total + y.left_subtree_length)
                return remove_left(root, at, total)
            if (at == total + y.left_subtree_length)
                return fuse(root.left(), root.right())
            return remove_right(root, at, total)
        }

        fun fuse(left: RedBlackTree, right: RedBlackTree): RedBlackTree {
            // match: (left, right)
            // case: (None, r)
            if (left.isEmpty()) return right
            if (right.isEmpty()) return left

            // match: (left.color, right.color)
            // case: (B, R)
            if (left.root_color() == Color.Black && right.root_color() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = fuse(left, right.left()),
                    data = right.root(),
                    rgt = right.right()
                )
            }

            // case: (R, B)
            if (left.root_color() == Color.Red && right.root_color() == Color.Black) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left(),
                    data = left.root(),
                    rgt = fuse(left.right(), right)
                )
            }

            // case: (R, R)
            if (left.root_color() == Color.Red && right.root_color() == Color.Red) {
                val fused = fuse(left.right(), right.left())
                if (!fused.isEmpty() && fused.root_color() == Color.Red) {
                    val new_left = RedBlackTree(
                        c = Color.Red,
                        lft = left.left(),
                        data = left.root(),
                        rgt = fused.left()
                    )
                    val new_right = RedBlackTree(
                        c = Color.Red,
                        lft = fused.right(),
                        data = right.root(),
                        rgt = right.right()
                    )
                    return RedBlackTree(
                        c = Color.Red,
                        lft = new_left,
                        data = fused.root(),
                        rgt = new_right
                    )
                }
                val new_right = RedBlackTree(
                    c = Color.Red,
                    lft = fused,
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left(),
                    data = left.root(),
                    rgt = new_right
                )
            }

            // case: (B, B)
            check(left.root_color() == right.root_color() && left.root_color() == Color.Black)
            val fused = fuse(left.right(), right.left())
            if (!fused.isEmpty() && fused.root_color() == Color.Red) {
                val new_left = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = fused.left()
                )
                val new_right = RedBlackTree(
                    c = Color.Black,
                    lft = fused.right(),
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = new_left,
                    data = fused.root(),
                    rgt = new_right
                )
            }
            val new_right = RedBlackTree(
                c = Color.Black,
                lft = fused,
                data = right.root(),
                rgt = right.right()
            )
            val new_node = RedBlackTree(
                c = Color.Red,
                lft = left.left(),
                data = left.root(),
                rgt = new_right
            )
            return balance_left(new_node)
        }

        fun balance_left(left: RedBlackTree): RedBlackTree {
            // match: (color_l, color_r, color_r_l)
            // case: (Some(R), ..)
            if (!left.left().isEmpty() && left.left().root_color() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left().paint(Color.Black),
                    data = left.root(),
                    rgt = left.right()
                )
            }

            // case: (_, Some(B), _)
            if (!left.right().isEmpty() && left.right().root_color() == Color.Black) {
                val new_left = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = left.right().paint(Color.Red)
                )
                return balance(new_left)
            }

            // case: (_, Some(R), Some(B))
            if (!left.right().isEmpty() && left.right().root_color() == Color.Red
                && !left.right().left().isEmpty() && left.right().left().root_color() == Color.Black) {
                val unbalanced_new_right = RedBlackTree(
                    c = Color.Black,
                    lft = left.right().left().right(),
                    data = left.right().root(),
                    rgt = left.right().right().paint(Color.Red)
                )
                val new_right = balance(unbalanced_new_right)
                val new_left = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = left.right().left().left()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = new_left,
                    data = left.right().left().root(),
                    rgt = new_right
                )
            }

            check(false) { "impossible!" }
            return left
        }

        fun balance_right(right: RedBlackTree): RedBlackTree {
            // match: (color_l, color_l_r, color_r)
            // case: (.., Some(R))
            if (!right.right().isEmpty() && right.right().root_color() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = right.left(),
                    data = right.root(),
                    rgt = right.right().paint(Color.Black)
                )
            }

            // case: (Some(B), ..)
            if (!right.left().isEmpty() && right.left().root_color() == Color.Black) {
                val new_right = RedBlackTree(
                    c = Color.Black,
                    lft = right.left().paint(Color.Red),
                    data = right.root(),
                    rgt = right.right()
                )
                return balance(new_right)
            }

            // case: (Some(R), Some(B), _)
            if (!right.left().isEmpty() && right.left().root_color() == Color.Red
                && !right.left().right().isEmpty() && right.left().right().root_color() == Color.Black) {
                val unbalanced_new_left = RedBlackTree(
                    c = Color.Black,
                    // Note: Because 'left' is red, it must have a left child.
                    lft = right.left().left().paint(Color.Red),
                    data = right.left().root(),
                    rgt = right.left().right().left()
                )
                val new_left = balance(unbalanced_new_left)
                val new_right = RedBlackTree(
                    c = Color.Black,
                    lft = right.left().right().right(),
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = new_left,
                    data = right.left().right().root(),
                    rgt = new_right
                )
            }

            check(false) { "impossible!" }
            return right
        }

        // Borrowed from https://github.com/dotnwat/persistent-rbtree/blob/master/tree.h:checkConsistency.
        fun check_black_node_invariant(node: RedBlackTree): Int {
            if (node.isEmpty())
                return 1
            if (node.root_color() == Color.Red &&
                ((!node.left().isEmpty() && node.left().root_color() == Color.Red)
                        || (!node.right().isEmpty() && node.right().root_color() == Color.Red))) {
                return 1
            }

            val l = check_black_node_invariant(node.left())
            val r = check_black_node_invariant(node.right())

            if (l != 0 && r != 0 && l != r)
                return 0

            if (l != 0 && r != 0)
                return if (node.root_color() == Color.Red) l else l + 1

            return 0
        }

        fun satisfies_rb_invariants(root: RedBlackTree) {
            // 1. Every node is either red or black.
            // 2. All NIL nodes (figure 1) are considered black.
            // 3. A red node does not have a red child.
            // 4. Every path from a given node to any of its descendant NIL nodes goes through the same number of black nodes.

            // The internal nodes in this RB tree can be totally black so we will not count them directly, we'll just track
            // odd nodes as either red or black.
            // Measure the number of black nodes we need to validate.
            if (root.isEmpty()
                || (root.left().isEmpty() && root.right().isEmpty()))
                return

            check(check_black_node_invariant(root) != 0)
        }

    }
}
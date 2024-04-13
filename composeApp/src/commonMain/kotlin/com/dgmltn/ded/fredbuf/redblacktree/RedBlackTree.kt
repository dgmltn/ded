package com.dgmltn.ded.fredbuf.redblacktree

import com.dgmltn.ded.fredbuf.piecetree.BufferMeta
import com.dgmltn.ded.fredbuf.editor.CharOffset
import com.dgmltn.ded.fredbuf.editor.Length

//TODO: make this generic, and replace NodeData with <T>?

class RedBlackTree(val rootPtr: Node? = null) {

    constructor(c: Color, lft: RedBlackTree, data: NodeData, rgt: RedBlackTree): this(Node(c, lft.rootPtr, attribute(data, lft), rgt.rootPtr))

    enum class Color {
        Red, Black, DoubleBlack;
        override fun toString() = name
    }

    data class NodeData(
        val piece: Piece,
        val leftSubtreeLength: Length = Length(),
        val leftSubtreeLfCount: LFCount = LFCount()
    )

    data class Node(val color: Color, val left: Node?, val data: NodeData, val right: Node?)

    data class WalkResult(
        val tree: RedBlackTree,
        val accumulatedOffset: CharOffset
    )

    fun isEmpty() = rootPtr == null

    fun root(): NodeData {
        check(!isEmpty()) { "Tree is empty!" }
        return rootPtr!!.data
    }

    fun left(): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(rootPtr!!.left)
    }

    fun right(): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(rootPtr!!.right)
    }

    fun rootColor(): Color {
        check(!isEmpty()) { "Tree is empty!" }
        return rootPtr!!.color
    }

    fun insert(x: NodeData, at: CharOffset): RedBlackTree {
        val t = ins(x, at, CharOffset(0))
        return RedBlackTree(Color.Black, t.left(), t.root(), t.right())
    }

    fun ins(x: NodeData, at: CharOffset, totalOffset: CharOffset): RedBlackTree {
        if (isEmpty())
            return RedBlackTree(Color.Red, RedBlackTree(), x, RedBlackTree())

        val y = root()
        if (at < totalOffset + y.leftSubtreeLength + y.piece.length)
            return balance(rootColor(), left().ins(x, at, totalOffset), y, right())

        return balance(rootColor(), left(), y, right().ins(x, at, totalOffset + y.leftSubtreeLength + y.piece.length))
    }

    fun doubledLeft(): Boolean =
        !isEmpty()
                && rootColor() == Color.Red
                && !left().isEmpty()
                && left().rootColor() == Color.Red

    fun doubledRight(): Boolean =
        !isEmpty()
                && rootColor() == Color.Red
                && !right().isEmpty()
                && right().rootColor() != Color.Red

    fun paint(c: Color): RedBlackTree {
        check(!isEmpty()) { "Tree is empty!" }
        return RedBlackTree(c, left(), root(), right())
    }

    fun treeLength(): Length =
        if (isEmpty()) Length(0) else root().leftSubtreeLength + root().piece.length + right().treeLength()

    fun treeLfCount(): LFCount =
        if (isEmpty()) LFCount(0) else root().leftSubtreeLfCount + root().piece.newlineCount + right().treeLfCount()

    fun computeBufferMeta(): BufferMeta =
        BufferMeta(
            lfCount = treeLfCount(),
            totalContentLength = treeLength()
        )

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

    fun checkSatisfiesRbInvariants() = checkSatisfiesRbInvariants(this)

    companion object {
        fun attribute(data: NodeData, left: RedBlackTree): NodeData =
            data.copy(
                leftSubtreeLength = left.treeLength(),
                leftSubtreeLfCount = left.treeLfCount()
            )

        fun balance(node: RedBlackTree): RedBlackTree {
            // Two red children.
            if (
                !node.left().isEmpty() && node.left().rootColor() == Color.Red
                && !node.right().isEmpty() && node.right().rootColor() == Color.Red
            ) {
                val l = node.left().paint(Color.Black)
                val r = node.right().paint(Color.Black)
                return RedBlackTree(Color.Red, l, node.root(), r)
            }

            check(node.rootColor() == Color.Black)

            return balance(node.rootColor(), node.left(), node.root(), node.right())
        }

        fun balance(c: Color, lft: RedBlackTree, x: NodeData, rgt: RedBlackTree): RedBlackTree =
            if (c == Color.Black && lft.doubledLeft())
                RedBlackTree(
                    c = Color.Red,
                    lft = lft.left().paint(Color.Black),
                    data = lft.root(),
                    rgt = RedBlackTree(Color.Black, lft.right(), x, rgt)
                )
            else if (c == Color.Black && lft.doubledRight())
                RedBlackTree(
                    c = Color.Red,
                    lft = RedBlackTree(Color.Black, lft.left(), lft.root(), lft.right().left()),
                    data = lft.right().root(),
                    rgt = RedBlackTree(Color.Black, lft.right().right(), x, rgt)
                )
            else if (c == Color.Black && rgt.doubledLeft())
                RedBlackTree(
                    c = Color.Red,
                    lft = RedBlackTree(Color.Black, lft, x, rgt.left().left()),
                    data = rgt.left().root(),
                    rgt = RedBlackTree(Color.Black, rgt.left().right(), rgt.root(), rgt.right())
                )
            else if (c == Color.Black && rgt.doubledRight())
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
                offset = offset + t.root().leftSubtreeLength + t.root().piece.length
                t = t.right()
            }
            // Add the final offset from the last right node.
            offset += t.root().leftSubtreeLength
            return WalkResult(tree = t, accumulatedOffset = offset)
        }

        fun removeLeft(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            val newLeft = rem(root.left(), at, total)
            val newNode = RedBlackTree(Color.Red, newLeft, root.root(), root.right())

            // In this case, the root was a red node and must've had at least two children.
            if (!root.left().isEmpty() && root.left().rootColor() == Color.Black)
                return balanceLeft(newNode)

            return newNode
        }

        fun removeRight(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            val y = root.root()
            val newRight = rem(root.right(), at, total + y.leftSubtreeLength + y.piece.length)
            val newNode = RedBlackTree(Color.Red, root.left(), root.root(), newRight)

            // In this case, the root was a red node and must've had at least two children.
            if (!root.right().isEmpty() && root.right().rootColor() == Color.Black)
                return balanceRight(newNode)

            return newNode
        }

        fun rem(root: RedBlackTree, at: CharOffset, total: CharOffset): RedBlackTree {
            if (root.isEmpty())
                return RedBlackTree()

            val y = root.root()
            if (at < total + y.leftSubtreeLength)
                return removeLeft(root, at, total)
            if (at == total + y.leftSubtreeLength)
                return fuse(root.left(), root.right())
            return removeRight(root, at, total)
        }

        fun fuse(left: RedBlackTree, right: RedBlackTree): RedBlackTree {
            // match: (left, right)
            // case: (None, r)
            if (left.isEmpty()) return right
            if (right.isEmpty()) return left

            // match: (left.color, right.color)
            // case: (B, R)
            if (left.rootColor() == Color.Black && right.rootColor() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = fuse(left, right.left()),
                    data = right.root(),
                    rgt = right.right()
                )
            }

            // case: (R, B)
            if (left.rootColor() == Color.Red && right.rootColor() == Color.Black) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left(),
                    data = left.root(),
                    rgt = fuse(left.right(), right)
                )
            }

            // case: (R, R)
            if (left.rootColor() == Color.Red && right.rootColor() == Color.Red) {
                val fused = fuse(left.right(), right.left())
                if (!fused.isEmpty() && fused.rootColor() == Color.Red) {
                    val newLeft = RedBlackTree(
                        c = Color.Red,
                        lft = left.left(),
                        data = left.root(),
                        rgt = fused.left()
                    )
                    val newRight = RedBlackTree(
                        c = Color.Red,
                        lft = fused.right(),
                        data = right.root(),
                        rgt = right.right()
                    )
                    return RedBlackTree(
                        c = Color.Red,
                        lft = newLeft,
                        data = fused.root(),
                        rgt = newRight
                    )
                }
                val newRight = RedBlackTree(
                    c = Color.Red,
                    lft = fused,
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left(),
                    data = left.root(),
                    rgt = newRight
                )
            }

            // case: (B, B)
            check(left.rootColor() == right.rootColor() && left.rootColor() == Color.Black)
            val fused = fuse(left.right(), right.left())
            if (!fused.isEmpty() && fused.rootColor() == Color.Red) {
                val newLeft = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = fused.left()
                )
                val newRight = RedBlackTree(
                    c = Color.Black,
                    lft = fused.right(),
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = newLeft,
                    data = fused.root(),
                    rgt = newRight
                )
            }
            val newRight = RedBlackTree(
                c = Color.Black,
                lft = fused,
                data = right.root(),
                rgt = right.right()
            )
            val newNode = RedBlackTree(
                c = Color.Red,
                lft = left.left(),
                data = left.root(),
                rgt = newRight
            )
            return balanceLeft(newNode)
        }

        private fun balanceLeft(left: RedBlackTree): RedBlackTree {
            // match: (color_l, color_r, color_r_l)
            // case: (Some(R), ..)
            if (!left.left().isEmpty() && left.left().rootColor() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = left.left().paint(Color.Black),
                    data = left.root(),
                    rgt = left.right()
                )
            }

            // case: (_, Some(B), _)
            if (!left.right().isEmpty() && left.right().rootColor() == Color.Black) {
                val newLeft = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = left.right().paint(Color.Red)
                )
                return balance(newLeft)
            }

            // case: (_, Some(R), Some(B))
            if (!left.right().isEmpty() && left.right().rootColor() == Color.Red
                && !left.right().left().isEmpty() && left.right().left().rootColor() == Color.Black
            ) {
                val unbalancedNewRight = RedBlackTree(
                    c = Color.Black,
                    lft = left.right().left().right(),
                    data = left.right().root(),
                    rgt = left.right().right().paint(Color.Red)
                )
                val newRight = balance(unbalancedNewRight)
                val newLeft = RedBlackTree(
                    c = Color.Black,
                    lft = left.left(),
                    data = left.root(),
                    rgt = left.right().left().left()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = newLeft,
                    data = left.right().left().root(),
                    rgt = newRight
                )
            }

            check(false) { "impossible!" }
            return left
        }

        private fun balanceRight(right: RedBlackTree): RedBlackTree {
            // match: (color_l, color_l_r, color_r)
            // case: (.., Some(R))
            if (!right.right().isEmpty() && right.right().rootColor() == Color.Red) {
                return RedBlackTree(
                    c = Color.Red,
                    lft = right.left(),
                    data = right.root(),
                    rgt = right.right().paint(Color.Black)
                )
            }

            // case: (Some(B), ..)
            if (!right.left().isEmpty() && right.left().rootColor() == Color.Black) {
                val newRight = RedBlackTree(
                    c = Color.Black,
                    lft = right.left().paint(Color.Red),
                    data = right.root(),
                    rgt = right.right()
                )
                return balance(newRight)
            }

            // case: (Some(R), Some(B), _)
            if (!right.left().isEmpty() && right.left().rootColor() == Color.Red
                && !right.left().right().isEmpty() && right.left().right().rootColor() == Color.Black
            ) {
                val unbalancedNewLeft = RedBlackTree(
                    c = Color.Black,
                    // Note: Because 'left' is red, it must have a left child.
                    lft = right.left().left().paint(Color.Red),
                    data = right.left().root(),
                    rgt = right.left().right().left()
                )
                val newLeft = balance(unbalancedNewLeft)
                val newRight = RedBlackTree(
                    c = Color.Black,
                    lft = right.left().right().right(),
                    data = right.root(),
                    rgt = right.right()
                )
                return RedBlackTree(
                    c = Color.Red,
                    lft = newLeft,
                    data = right.left().right().root(),
                    rgt = newRight
                )
            }

            check(false) { "impossible!" }
            return right
        }

        // Borrowed from https://github.com/dotnwat/persistent-rbtree/blob/master/tree.h:checkConsistency.
        private fun checkBlackNodeInvariant(node: RedBlackTree): Int {
            if (node.isEmpty())
                return 1
            if (node.rootColor() == Color.Red &&
                ((!node.left().isEmpty() && node.left().rootColor() == Color.Red)
                        || (!node.right().isEmpty() && node.right().rootColor() == Color.Red))) {
                return 1
            }

            val l = checkBlackNodeInvariant(node.left())
            val r = checkBlackNodeInvariant(node.right())

            if (l != 0 && r != 0 && l != r)
                return 0

            if (l != 0 && r != 0)
                return if (node.rootColor() == Color.Red) l else l + 1

            return 0
        }

        fun checkSatisfiesRbInvariants(root: RedBlackTree) {
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

            check(checkBlackNodeInvariant(root) != 0)
        }

    }
}
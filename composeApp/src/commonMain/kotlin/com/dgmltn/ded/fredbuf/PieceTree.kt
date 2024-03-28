package com.dgmltn.ded.fredbuf

import androidx.compose.ui.node.Ref

data class UndoRedoEntry(val root: RedBlackTree, val op_offset: CharOffset)

typealias UndoStack = List<UndoRedoEntry>
typealias RedoStack = List<UndoRedoEntry>

data class LineStart(val value: Int)

data class NodePosition(
    val node: RedBlackTree.NodeData? = null,
    val remainder: Length,
    val start_offset: CharOffset,
    val line: Line
)

data class CharBuffer(
    val buffer: String,
    val line_starts: LineStart
)

typealias BufferReference = CharBuffer

typealias Buffers = List<BufferReference>

data class BufferCollection(
    val buffer_at: CharBuffer,
    val orig_buffers: Buffers,
    val mod_buffer: CharBuffer
) {
    fun buffer_offset(index: BufferIndex, cursor: BufferCursor): CharOffset {
        TODO()
    }
}

data class LineRange(
    val first: CharOffset,
    val last: CharOffset  // Does not include LF.
)

data class UndoRedoResult(
    val success: Boolean,
    val op_offset: CharOffset
)

// Owning snapshot owns its own buffer data (performs a lightweight copy) so
// that even if the original tree is destroyed, the owning snapshot can still
// reference the underlying text.
class OwningSnapshot(
    tree: Tree,
    dt: RedBlackTree = RedBlackTree()
) {
    private lateinit var root: RedBlackTree
    private lateinit var meta: BufferMeta
    // This should be fairly lightweight.  The original buffers
    // will retain the majority of the memory consumption.
    private lateinit var buffers: BufferCollection

    // Queries.
    fun get_line_content(buf: String, line: Line) {
        TODO()
    }
    fun get_line_content_crlf(buf: String, line: Line): IncompleteCRLF {
        TODO()
    }
    fun line_at(offset: CharOffset): Line {
        TODO()
    }
    fun get_line_range(line: Line): LineRange {
        TODO()
    }
    fun get_line_range_crlf(line: Line): LineRange {
        TODO()
    }
    fun get_line_range_with_newline(line: Line): LineRange {
        TODO()
    }
    fun is_empty(): Boolean {
        return meta.total_content_length.value == 0
    }
    fun line_count(): Length {
        return Length(meta.lf_count.value + 1)
    }
}

// Reference snapshot owns no data and is only valid for as long as the original
// tree buffers are valid.
// TODO: this is pretty much a copy of OwningSnapshot. Maybe just delete this one
class ReferenceSnapshot(
    tree: Tree,
    dt: RedBlackTree = RedBlackTree()
) {
    private lateinit var root: RedBlackTree
    private lateinit var meta: BufferMeta
    // A reference to the underlying tree buffers.
    private lateinit var buffers: BufferCollection

    // Queries.
    fun get_line_content(buf: String, line: Line) {
        TODO()
    }
    fun get_line_content_crlf(buf: String, line: Line): IncompleteCRLF {
        TODO()
    }
    fun line_at(offset: CharOffset): Line {
        TODO()
    }
    fun get_line_range(line: Line): LineRange {
        TODO()
    }
    fun get_line_range_crlf(line: Line): LineRange {
        TODO()
    }
    fun get_line_range_with_newline(line: Line): LineRange {
        TODO()
    }
    fun is_empty(): Boolean {
        return meta.total_content_length.value == 0
    }
    fun line_count(): Length {
        return Length(meta.lf_count.value + 1)
    }
}

// When mutating the tree nodes are saved by default into the undo stack.  This
// allows callers to suppress this behavior.
enum class SuppressHistory { No, Yes }

data class BufferMeta(
    val lf_count: LFCount,
    val total_content_length: Length
)

// Indicates whether or not line was missing a CR (e.g. only a '\n' was at the end).
enum class IncompleteCRLF { No, Yes }

class Tree(
    buffers: List<Buffers> = emptyList() //TODO: this is Buffers&&, is List<Buffers> correct?
) {
    private lateinit var buffers: BufferCollection
    //Buffers buffers;
    //CharBuffer mod_buffer;
    private lateinit var root: RedBlackTree
    private lateinit var scratch_starts: List<LineStart>
    private lateinit var last_insert: BufferCursor
    // Note: This is absolute position. Initialize to nonsense value.
    private val end_last_insert: CharOffset = CharOffset.Sentinel
    private lateinit var meta: BufferMeta
    private lateinit var undo_stack: UndoStack
    private lateinit var redo_stack: RedoStack


    // Interface.
    // Initialization after populating initial immutable buffers from ctor.
    fun build_tree() {
        TODO()
    }

    fun insert(
        offset: CharOffset,
        txt: String,
        suppress_history: SuppressHistory = SuppressHistory.No
    ) {
        TODO()
    }

    fun remove(
        offset: CharOffset,
        count: Length,
        suppress_history: SuppressHistory = SuppressHistory.No
    ) {
        TODO()
    }

    fun try_undo(op_offset: CharOffset) {
        TODO()
    }

    fun try_redo(op_offset: CharOffset) {
        TODO()
    }

    // Direct history manipulation.
    // This will commit the current node to the history.  The offset provided will be the undo point later.
    fun commit_head(offset: CharOffset) {
        TODO()
    }

    fun head(): RedBlackTree {
        TODO()
    }

    // Snaps the tree back to the specified root.  This needs to be called with a root that is derived from
    // the set of buffers based on its creation.
    fun snap_to(new_root: RedBlackTree) {
        TODO()
    }

    // Queries.
    fun get_line_content(buf: String, line: Line) {
        TODO()
    }

    fun get_line_content_crlf(buf: String, line: Line): IncompleteCRLF {
        TODO()
    }

    fun at(offset: CharOffset): Char {
        TODO()
    }

    fun line_at(offset: CharOffset): Line {
        TODO()
    }

    fun get_line_range(line: Line): LineRange {
        TODO()
    }

    fun get_line_range_crlf(line: Line): LineRange {
        TODO()
    }

    fun get_line_range_with_newline(line: Line): LineRange {
        TODO()
    }

    fun length(): Length {
        return meta.total_content_length
    }

    fun is_empty(): Boolean {
        return meta.total_content_length.value == 0
    }

    fun line_feed_count(): LFCount {
        return meta.lf_count
    }

    fun line_count(): Length {
        return Length(line_feed_count().value + 1)
    }

    fun owning_snap(): OwningSnapshot {
        TODO()
    }

    fun ref_snap(): ReferenceSnapshot {
        TODO()
    }

    // Privates
    private fun print_piece(piece: Piece, tree: Tree, level: Int) {
        TODO()
    }

    private fun print_tree(tree: Tree) {
        TODO()
    }

    private fun internal_insert(offset: CharOffset, txt: String) {
        TODO()
    }

    private fun internal_remove(offset: CharOffset, count: Length) {
        TODO()
    }

    //TODO: what is this? using Accumulator = Length(*)(const BufferCollection*, const Piece&, Line);
    //TODO: what is this? template <Accumulator accumulate>

    private fun line_start(offset: CharOffset, buffers: BufferCollection, node: RedBlackTree, line: Line) {
        TODO()
    }

    private fun line_end_crlf(offset: CharOffset, buffers: BufferCollection, root: RedBlackTree, node: RedBlackTree, line: Line) {
        TODO()
    }

    private fun accumulate_value(buffers: BufferCollection, piece: Piece, index: Line): Length {
        TODO()
    }

    private fun accumulate_value_no_lf(buffers: BufferCollection, piece: Piece, index: Line): Length {
        TODO()
    }

    private fun populate_from_node(buf: String, buffers: BufferCollection, node: RedBlackTree) {
        TODO()
    }

    private fun populate_from_node(buf: String, buffers: BufferCollection, node: RedBlackTree, line_index: Line) {
        TODO()
    }

    private fun line_feed_count(buffers: BufferCollection, index: BufferIndex, start: BufferCursor, end: BufferCursor): LFCount {
        TODO()
    }

    private fun node_at(buffers: BufferCollection, node: RedBlackTree, off: CharOffset): NodePosition {
        TODO()
    }

    private fun buffer_position(buffers: BufferCollection, piece: Piece, remainder: Length): BufferCursor {
        TODO()
    }

    private fun char_at(buffers: BufferCollection, node: RedBlackTree, offset: CharOffset): Char {
        TODO()
    }

    private fun trim_piece_right(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        TODO()
    }

    private fun trim_piece_left(buffers: BufferCollection, piece: Piece, pos: BufferCursor): Piece {
        TODO()
    }

    data class ShrinkResult(val left: Piece, val right: Piece)

    private fun shrink_piece(buffers: BufferCollection, piece: Piece, first: BufferCursor, last: BufferCursor) {
        TODO()
    }

    // Direct mutations.

    private fun assemble_line(buf: String, node: RedBlackTree, line: Line) {
        TODO()
    }

    private fun build_piece(txt: String): Piece {
        TODO()
    }

    private fun combine_pieces(existing_piece: NodePosition, new_piece: Piece) {
        TODO()
    }

    private fun remove_node_range(first: NodePosition, length: Length) {
        TODO()
    }

    private fun compute_buffer_meta() {
        TODO()
    }

    private fun append_undo(old_root: RedBlackTree, op_offset: CharOffset) {
        TODO()
    }
}

class TreeWalker {
    private lateinit var buffers: BufferCollection
    private lateinit var root: RedBlackTree
    private lateinit var meta: BufferMeta
    private lateinit var stack: List<StackEntry>
    private val total_offset: CharOffset = CharOffset(0)
    private val first_ptr: Char = nullptr
    private val last_ptr: Char = nullptr

    constructor(tree: Tree) {
        TODO()
    }
    constructor(snap: OwningSnapshot, offset: CharOffset = CharOffset(0)) {
        TODO()
    }
    constructor(snap: ReferenceSnapshot, offset: CharOffset = CharOffset(0)) {
        TODO()
    }

    fun current(): Char {
        TODO()
    }
    fun next(): Char {
        TODO()
    }
    fun seek(offset: CharOffset) {
        TODO()
    }
    fun exhausted(): Boolean {
        TODO()
    }
    fun remaining(): Length {
        TODO()
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
        TODO()
    }

    private fun fast_forward_to(offset: CharOffset) {
        TODO()
    }

    enum class Direction {
        Left, Center, Right
    }

    data class StackEntry(
        val node: RedBlackTree,
        val direction: Direction = Direction.Left
    )
}

class ReverseTreeWalker {
    constructor(tree: Tree, offset: CharOffset = CharOffset(0)) {
        TODO()
    }
    constructor(snap: OwningSnapshot, offset: CharOffset = CharOffset(0)) {
        TODO()
    }
    constructor(snap: ReferenceSnapshot, offset: CharOffset = CharOffset(0)) {
        TODO()
    }

    private lateinit var buffers: BufferCollection
    private lateinit var root: RedBlackTree
    private lateinit var meta: BufferMeta
    private lateinit var stack: List<TreeWalker.StackEntry>
    private val total_offset: CharOffset = CharOffset(0)
    private val first_ptr: Char = nullptr
    private val last_ptr: Char = nullptr


    fun current(): Char {
        TODO()
    }
    fun next(): Char {
        TODO()
    }
    fun seek(offset: CharOffset) {
        TODO()
    }
    fun exhausted(): Boolean {
        TODO()
    }
    fun remaining(): Length {
        TODO()
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
        TODO()
    }

    private fun fast_forward_to(offset: CharOffset) {
        TODO()
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
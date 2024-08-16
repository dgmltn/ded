package com.dgmltn.ded.editor

class LinePositionTracker(private val text: StringBuilder) {

    var rowCount: Int = text.countNewlines() + 1
        private set

    private var newlineIndices = mutableListOf<Int>()

    private var size = 0

    // Invalidate newline indices at/after position [atPosition]
    fun invalidateIndices(atRow: Int) {
        size = atRow
    }

    // Get the position of newline number x
    fun newline(index: Int): Int {
        require(index < rowCount - 1) { "index out of bounds" }

        while(size < rowCount - 1 && size <= index) {
            val next = if (newlineIndices.isEmpty())
                text.nextNewline()
            else
                text.nextNewline(newlineIndices.last() + 1)
            newlineIndices.add(next)
            size++
        }
        return newlineIndices[index]
    }

    fun getRowOf(position: Int): Int {
        require(position >= 0 && position <= text.length) { "Position out of bounds" }

        if (position == 0) return 0
        if (rowCount == 1) return 0

        // newlineIndices already contains this position somewhere, binary search for it
        if (newlineIndices.isNotEmpty() && newlineIndices.last() >= position) {
            // Find the index of the largest newline index less than or equal to the given position
            // Via fast binary search
            val lineStartIndex = newlineIndices.binarySearch(position)
            val actualLineStartIndex =
                if (lineStartIndex >= 0) (lineStartIndex - 1) else (-lineStartIndex - 1) - 1

            return actualLineStartIndex + 1
        }

        // newlineIndices does not contain this position, count up until found
        while (size < rowCount - 1) {
            val nextIndex = size
            val nextNewline = newline(nextIndex)
            if (nextNewline >= position) {
                return nextIndex
            }
        }
        val lastIndex = rowCount - 2
        val lastNewline = newline(lastIndex)
        return if (lastNewline < position)
            rowCount - 1
        else
            rowCount - 2

    }

    fun getRangeOfRow(row: Int): IntRange {
        require(row in 0..rowCount) { "row $row is out of bounds. Should be in 0..$rowCount" }

        val length = text.length
        val lastRowIndex = rowCount - 1
        val endsWithNewline = text.endsWithNewline()

        return when {
            // 0 length
            length == 0 -> 0 .. 0
            // no newlines
            rowCount == 1 -> 0 until length //TODO: is this right?
            // first line
            row == 0 -> 0 .. newline(0)
            // last line of a file that ends with newline
            row == lastRowIndex && endsWithNewline -> length .. length
            // last line of a file that does not end with newline
            row == lastRowIndex -> (newline(row - 1) + 1) until length
            // middle line
            else -> (newline(row - 1) + 1) .. newline(row)
        }
    }

    fun addNewlines(count: Int) {
        rowCount += count
    }

    private fun StringBuilder.nextNewline(after: Int = 0) = indexOf('\n', after)
    private fun StringBuilder.countNewlines() = count { it == '\n' }
    private fun StringBuilder.endsWithNewline() = endsWith('\n')
}
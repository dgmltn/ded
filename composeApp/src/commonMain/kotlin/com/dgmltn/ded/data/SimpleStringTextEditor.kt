package com.dgmltn.ded.data

class SimpleStringTextEditor : TextEditor{

    private var inner_content = ""
    private var cursor = 0

    override fun moveCursorToStart() {
        moveCursor(0)
    }

    override fun moveCursorToEnd() {
        moveCursor(inner_content.length)
    }

    override fun moveCursor(position: Int) {
        cursor = position
    }

    override fun insert(content: String) {
        inner_content = if(inner_content.isEmpty()) {
            content
        } else {
            val contentBeforeCursor = inner_content.substring(startIndex = 0, endIndex = cursor)
            val contentAfterCursor = when {
                cursor >= inner_content.length -> ""
                else -> inner_content.substring(startIndex = cursor, endIndex = inner_content.length)
            }
            "$contentBeforeCursor$content$contentAfterCursor"
        }
        cursor += content.length
    }

    override fun delete(n: Int) {
        val removeFrom = (cursor - n).coerceAtLeast(0)
        val removeTo = cursor
        inner_content = inner_content.removeRange(removeFrom until removeTo)
        cursor = removeFrom
    }

    override fun content(): String {
        return inner_content
    }
}
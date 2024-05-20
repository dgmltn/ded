package com.dgmltn.ded.editor

data class RowCol(val row: Int, val col: Int) {
    companion object {
        val Zero = RowCol(0, 0)
    }
}
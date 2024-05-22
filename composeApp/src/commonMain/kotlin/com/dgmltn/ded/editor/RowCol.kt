package com.dgmltn.ded.editor

import androidx.compose.ui.unit.IntOffset

data class RowCol(val row: Int, val col: Int) {
    companion object {
        val Zero = RowCol(0, 0)
    }
}

fun IntOffset.toRowCol() = RowCol(y, x)
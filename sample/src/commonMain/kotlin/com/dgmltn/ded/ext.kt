package com.dgmltn.ded

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue

fun IntSize.div(other: IntSize): IntSize {
    return IntSize(width / other.width, height / other.height)
}

fun Offset.div(other: IntSize) = Offset(x / other.width, y / other.height)
fun Offset.toInt() = IntOffset(x.toInt(), y.toInt())

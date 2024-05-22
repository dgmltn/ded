package com.dgmltn.ded

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue
import kotlin.math.log

/**
 * The number of digits in this number (in base 10)
 */
fun Int.numDigits() = log(toDouble(), 10.0).toInt() + 1


fun IntSize.div(other: IntSize): IntSize {
    return IntSize(width / other.width, height / other.height)
}

fun Offset.div(other: IntSize) = Offset(x / other.width, y / other.height)
fun Offset.toInt() = IntOffset(x.toInt(), y.toInt())

fun IntProgression.toIntRange(): IntRange {
    if (step.absoluteValue != 1) throw IllegalArgumentException("Only step 1 is supported")
    return if (first <= last) first..last else last .. first
}
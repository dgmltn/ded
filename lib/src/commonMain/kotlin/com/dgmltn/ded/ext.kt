package com.dgmltn.ded

import kotlin.math.absoluteValue


fun IntProgression.toIntRange(): IntRange {
    if (step.absoluteValue != 1) throw IllegalArgumentException("Only step 1 is supported")
    return if (first <= last) first..last else last .. first
}
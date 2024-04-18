package com.dgmltn.ded

import kotlin.math.log

/**
 * The number of digits in this number (in base 10)
 */
fun Int.numDigits() = log(toDouble(), 10.0).toInt() + 1
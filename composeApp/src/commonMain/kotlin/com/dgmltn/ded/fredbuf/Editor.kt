package com.dgmltn.ded.fredbuf

data class Column(val value: Int = 0) {
    companion object {
        val Beginning = Column(-1)
    }
}

data class Length(val value: Int = 0) {
    operator fun plus(other: Length) = Length(this.value + other.value)

    operator fun minus(length: Length) = Length(this.value - length.value)

}

data class CharOffset(val value: Int = 0) {
    companion object {
        val Sentinel = CharOffset(-1)
    }

    operator fun plus(length: Length) = CharOffset(this.value + length.value)

    fun distance(other: CharOffset) = Length((other - this).value)

    operator fun plus(other: CharOffset) = CharOffset(this.value + other.value)

    operator fun plus(other: Int) = CharOffset(this.value + other)

    operator fun minus(other: CharOffset) = CharOffset(this.value - other.value)

    operator fun minus(length: Length) = CharOffset(this.value - length.value)

    operator fun minus(other: Int) = CharOffset(this.value - other)

    operator fun compareTo(other: CharOffset) = this.value - other.value
}

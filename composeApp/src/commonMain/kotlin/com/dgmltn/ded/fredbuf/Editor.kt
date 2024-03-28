package com.dgmltn.ded.fredbuf

data class Column(val value: Int) {
    companion object {
        val Beginning = Column(-1)
    }
}

data class Length(val value: Int) {
    operator fun plus(other: Length) = Length(this.value + other.value)
}

data class CharOffset(val value: Int) {
    companion object {
        val Sentinel = CharOffset(-1)
    }

    operator fun plus(length: Length) = CharOffset(this.value + length.value)

    fun distance(other: CharOffset) = other - this

    operator fun plus(other: CharOffset) = CharOffset(this.value + other.value)

    operator fun minus(other: CharOffset) = CharOffset(this.value - other.value)

    operator fun compareTo(other: CharOffset) = this.value - other.value
}

package com.dgmltn.ded.fredbuf.editor

data class CharOffset(val value: Int = 0) {
    companion object {
        val Sentinel = CharOffset(Int.MAX_VALUE)
    }

    operator fun plus(length: Length) = CharOffset(this.value + length.value)

    fun distance(other: CharOffset) = Length(other.value - this.value)

    operator fun plus(other: CharOffset) = CharOffset(this.value + other.value)

    operator fun plus(other: Int) = CharOffset(this.value + other)

    operator fun minus(other: CharOffset) = CharOffset(this.value - other.value)

    operator fun minus(length: Length) = CharOffset(this.value - length.value)

    operator fun minus(other: Int) = CharOffset(this.value - other)

    operator fun compareTo(other: CharOffset) = this.value - other.value

    override fun toString(): String =
        if (this == Sentinel) "Sentinel" else value.toString()
}

data class Column(val value: Int = 0) {
    companion object {
        val Beginning = Column(-1)
    }

    override fun toString(): String =
        if (this == Beginning) "Beginning" else value.toString()
}


data class Length(val value: Int = 0) {
    operator fun plus(other: Length) = Length(this.value + other.value)

    operator fun minus(length: Length) = Length(this.value - length.value)

    override fun toString(): String =
        value.toString()
}
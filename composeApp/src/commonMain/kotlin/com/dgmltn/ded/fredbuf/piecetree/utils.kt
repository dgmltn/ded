package com.dgmltn.ded.fredbuf.piecetree

fun trim_crlf(buf: StringBuilder, walker: TreeWalker): IncompleteCRLF {
    var prev_char: Char? = null
    while (!walker.exhausted()) {
        val c = walker.next()
        if (c == '\n') {
            if (prev_char == '\r') {
                buf.deleteAt(buf.length - 1)
                return IncompleteCRLF.No
            }
            return IncompleteCRLF.Yes
        }
        buf.append(c)
        prev_char = c
    }
    // End of the buffer is not an incomplete CRLF.
    return IncompleteCRLF.No
}


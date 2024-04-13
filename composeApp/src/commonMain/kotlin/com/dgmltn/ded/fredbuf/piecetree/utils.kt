package com.dgmltn.ded.fredbuf.piecetree

fun trimCrlf(buf: StringBuilder, walker: TreeWalker): IncompleteCRLF {
    var prevChar: Char? = null
    while (!walker.exhausted()) {
        val c = walker.next()
        if (c == '\n') {
            if (prevChar == '\r') {
                buf.deleteAt(buf.length - 1)
                return IncompleteCRLF.No
            }
            return IncompleteCRLF.Yes
        }
        buf.append(c)
        prevChar = c
    }
    // End of the buffer is not an incomplete CRLF.
    return IncompleteCRLF.No
}


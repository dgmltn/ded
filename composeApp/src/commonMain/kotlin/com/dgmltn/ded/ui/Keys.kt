package com.dgmltn.ded.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key


data class ModifiedKey(
    val key: Key,
    val shift: Boolean = false,
    val meta: Boolean = false,
    val ctrl: Boolean = false,
    val alt: Boolean = false
) {
    constructor(keyEvent: KeyEvent) : this(
        key = keyEvent.key,
        shift = keyEvent.isShiftPressed,
        meta = keyEvent.isMetaPressed,
        ctrl = keyEvent.isCtrlPressed,
        alt = keyEvent.isAltPressed
    )
}

fun Key.modified(
    shift: Boolean = false,
    meta: Boolean = false,
    ctrl: Boolean = false,
    alt: Boolean = false
) =
    ModifiedKey(this, shift, meta, ctrl, alt)

val keys: Map<ModifiedKey, DedState.() -> Boolean> = mapOf(
    // Movement
    Key.DirectionLeft.modified() to { moveBack() },
    Key.DirectionRight.modified() to { moveFwd() },
    Key.DirectionDown.modified() to { moveNextRow() },
    Key.DirectionUp.modified() to { movePrevRow() },

    // Spacing/newlines
    Key.Spacebar.modified() to { insert(" ") },
    Key.Tab.modified() to {
        val tabSize = languageConfig.tabSize
        val col = getRowColOfCursor().col
        val numOfSpaces = tabSize - (col % tabSize)
        insert(" ".repeat(numOfSpaces))
    },
    Key.Enter.modified() to { insert("\n") },
    Key.Backspace.modified() to { backspace() },
    Key.Delete.modified() to { delete(1) == 1 },

    // Undo/redo
    Key.Z.modified(meta = true) to { undo() },
    Key.Z.modified(meta = true, shift = true) to { redo() },

    // Brackets
    Key.LeftBracket.modified() to { insert("[") },
    Key.RightBracket.modified() to { insert("]") },
    Key.LeftBracket.modified(shift = true) to { insert("{") },
    Key.RightBracket.modified(shift = true) to { insert("}") },

    // Letters
    Key.A.modified() to { insert("a") },
    Key.A.modified(shift = true) to { insert("A") },
    Key.B.modified() to { insert("b") },
    Key.B.modified(shift = true) to { insert("B") },
    Key.C.modified() to { insert("c") },
    Key.C.modified(shift = true) to { insert("C") },
    Key.D.modified() to { insert("d") },
    Key.D.modified(shift = true) to { insert("D") },
    Key.E.modified() to { insert("e") },
    Key.E.modified(shift = true) to { insert("E") },
    Key.F.modified() to { insert("f") },
    Key.F.modified(shift = true) to { insert("F") },
    Key.G.modified() to { insert("g") },
    Key.G.modified(shift = true) to { insert("G") },
    Key.H.modified() to { insert("h" ) },
    Key.H.modified(shift = true) to { insert("H") },
    Key.I.modified() to { insert("i") },
    Key.I.modified(shift = true) to { insert("I") },
    Key.J.modified() to { insert("j") },
    Key.J.modified(shift = true) to { insert("J") },
    Key.K.modified() to { insert("k") },
    Key.K.modified(shift = true) to { insert("K") },
    Key.L.modified() to { insert("l") },
    Key.L.modified(shift = true) to { insert("L") },
    Key.M.modified() to { insert("m") },
    Key.M.modified(shift = true) to { insert("M") },
    Key.N.modified() to { insert("n") },
    Key.N.modified(shift = true) to { insert("N") },
    Key.O.modified() to { insert("o") },
    Key.O.modified(shift = true) to { insert("O") },
    Key.P.modified() to { insert("p") },
    Key.P.modified(shift = true) to { insert("P") },
    Key.Q.modified() to { insert("q") },
    Key.Q.modified(shift = true) to { insert("Q") },
    Key.R.modified() to { insert("r") },
    Key.R.modified(shift = true) to { insert("R") },
    Key.S.modified() to { insert("s") },
    Key.S.modified(shift = true) to { insert("S") },
    Key.T.modified() to { insert("t") },
    Key.T.modified(shift = true) to { insert("T") },
    Key.U.modified() to { insert("u") },
    Key.U.modified(shift = true) to { insert("U") },
    Key.V.modified() to { insert("v") },
    Key.V.modified(shift = true) to { insert("V") },
    Key.W.modified() to { insert("w") },
    Key.W.modified(shift = true) to { insert("W") },
    Key.X.modified() to { insert("x") },
    Key.X.modified(shift = true) to { insert("X") },
    Key.Y.modified() to { insert("y") },
    Key.Y.modified(shift = true) to { insert("Y") },
    Key.Z.modified() to { insert("z") },
    Key.Z.modified(shift = true) to { insert("Z") },

    // Numbers
    Key.One.modified() to { insert("1") },
    Key.One.modified(shift = true) to { insert("!") },
    Key.Two.modified() to { insert("2") },
    Key.Two.modified(shift = true) to { insert("@") },
    Key.Three.modified() to { insert("3") },
    Key.Three.modified(shift = true) to { insert("#") },
    Key.Four.modified() to { insert("4") },
    Key.Four.modified(shift = true) to { insert("$") },
    Key.Five.modified() to { insert("5") },
    Key.Five.modified(shift = true) to { insert("%") },
    Key.Six.modified() to { insert("6") },
    Key.Six.modified(shift = true) to { insert("^") },
    Key.Seven.modified() to { insert("7") },
    Key.Seven.modified(shift = true) to { insert("&") },
    Key.Eight.modified() to { insert("8") },
    Key.Eight.modified(shift = true) to { insert("*") },
    Key.Nine.modified() to { insert("9") },
    Key.Nine.modified(shift = true) to { insert("(") },
    Key.Zero.modified() to { insert("0") },
    Key.Zero.modified(shift = true) to { insert(")") }
)

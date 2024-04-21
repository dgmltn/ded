package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.editor.StringBuilderEditor

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val dedState = rememberDedState(editor = StringBuilderEditor().apply {
        insert("Hello, world\nthis is a test\nLine #3")
        moveTo(5)
    })

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
            .focusRequester(focusRequester)
            .focusable()
            .dedKeyEvent(dedState)
    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code,
            colors = colors
        )
    }
}

@Composable
fun Modifier.dedKeyEvent(dedState: DedState) = this.then(
    onKeyEvent {
        Logger.e("DOUG: $it")
        if (it.type == KeyEventType.KeyDown) {
            when (it.key) {
                Key.DirectionLeft -> dedState.moveBack()
                Key.DirectionRight -> dedState.moveFwd()
                Key.DirectionDown -> dedState.moveNextRow()
                Key.DirectionUp -> dedState.movePrevRow()

                Key.Spacebar -> dedState.insert(" ")
                Key.Tab -> {
                    val col = dedState.getRowColOfCursor().col
                    // 0 -> 4, 1 -> 3, 2 -> 2, 3 -> 1
                    val numOfSpaces = 4 - (col % 4)
                    dedState.insert(" ".repeat(numOfSpaces))
                }

                Key.Enter -> dedState.insert("\n")
                Key.Backspace -> dedState.backspace()
                Key.Delete -> dedState.delete(1)

                Key.A -> dedState.insert(if (it.isShiftPressed) "A" else "a")
                Key.B -> dedState.insert(if (it.isShiftPressed) "B" else "b")
                Key.C -> dedState.insert(if (it.isShiftPressed) "C" else "c")
                Key.D -> dedState.insert(if (it.isShiftPressed) "D" else "d")
                Key.E -> dedState.insert(if (it.isShiftPressed) "E" else "e")
                Key.F -> dedState.insert(if (it.isShiftPressed) "F" else "f")
                Key.G -> dedState.insert(if (it.isShiftPressed) "G" else "g")
                Key.H -> dedState.insert(if (it.isShiftPressed) "H" else "h")
                Key.I -> dedState.insert(if (it.isShiftPressed) "I" else "i")
                Key.J -> dedState.insert(if (it.isShiftPressed) "J" else "j")
                Key.K -> dedState.insert(if (it.isShiftPressed) "K" else "k")
                Key.L -> dedState.insert(if (it.isShiftPressed) "L" else "l")
                Key.M -> dedState.insert(if (it.isShiftPressed) "M" else "m")
                Key.N -> dedState.insert(if (it.isShiftPressed) "N" else "n")
                Key.O -> dedState.insert(if (it.isShiftPressed) "O" else "o")
                Key.P -> dedState.insert(if (it.isShiftPressed) "P" else "p")
                Key.Q -> dedState.insert(if (it.isShiftPressed) "Q" else "q")
                Key.R -> dedState.insert(if (it.isShiftPressed) "R" else "r")
                Key.S -> dedState.insert(if (it.isShiftPressed) "S" else "s")
                Key.T -> dedState.insert(if (it.isShiftPressed) "T" else "t")
                Key.U -> dedState.insert(if (it.isShiftPressed) "U" else "u")
                Key.V -> dedState.insert(if (it.isShiftPressed) "V" else "v")
                Key.W -> dedState.insert(if (it.isShiftPressed) "W" else "w")
                Key.X -> dedState.insert(if (it.isShiftPressed) "X" else "x")
                Key.Y -> dedState.insert(if (it.isShiftPressed) "Y" else "y")
                Key.Z -> {
                    if (it.isMetaPressed) dedState.undo()
                    else dedState.insert(if (it.isShiftPressed) "Z" else "z")
                }

                Key.LeftBracket -> dedState.insert(if (it.isShiftPressed) "{" else "[")
                Key.RightBracket -> dedState.insert(if (it.isShiftPressed) "}" else "]")
            }
        }
        true
    }
)
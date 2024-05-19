package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.editor.language.JavascriptLanguageConfig

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor
) {
    val focusRequester = remember { FocusRequester() }
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val dedState = rememberDedState(
        editor = StringBuilderEditor()
            .apply {
                insert("Hello, world\nthis is a test\nLine #3")
                moveTo(5)
            },
        languageConfig = JavascriptLanguageConfig()
    )

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
            .focusRequester(focusRequester)
            .focusable()
            .dedKeyEvent(dedState)
            .detectSelectGestures(dedState)

    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code,
            colors = colors
        )

        Text(
            modifier = Modifier.align(Alignment.BottomEnd),
            text = dedState.cursorPos.toString(),
        )
    }
}

@Composable
fun Modifier.dedKeyEvent(dedState: DedState) = then(
    onKeyEvent {
        Logger.e("DOUG: $it")
        (it.type == KeyEventType.KeyDown)
                && keys[ModifiedKey(it)]?.invoke(dedState) ?: false
    }
)

@Composable
fun Modifier.detectSelectGestures(
    dedState: DedState,
) = this.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { offset ->
            val col = (offset.x / dedState.cellSize.width).toInt() - dedState.cellOffset.x
            val row = ((offset.y / dedState.cellSize.height).toInt() - dedState.cellOffset.y).coerceIn(0, dedState.lineCount - 1)
            dedState.moveTo(RowCol(row, col))
        },
        onDrag = { change, dragAmount ->
            val col = (change.position.x / dedState.cellSize.width).toInt() - dedState.cellOffset.x
            val row = ((change.position.y / dedState.cellSize.height).toInt() - dedState.cellOffset.y).coerceIn(0, dedState.lineCount - 1)
            dedState.moveTo(RowCol(row, col))
            change.consume()
        },
    )
}
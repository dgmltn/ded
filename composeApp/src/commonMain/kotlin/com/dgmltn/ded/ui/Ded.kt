package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.editor.StringBuilderEditor

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor
) {
    val dedState = rememberDedState(editor = StringBuilderEditor().apply {
        insert("Hello, world\nthis is a test\nLine #3")
        moveTo(5)
    })

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
            .focusable(true)
            .clickable {
                Logger.e("DOUG: click")
            }
            .onKeyEvent {
                Logger.e("DOUG: $it")
                if (it.type == KeyEventType.KeyDown) {
                    when (it.key) {
                        Key.DirectionLeft -> dedState.moveBack()
                        Key.DirectionRight -> dedState.moveFwd()
                        Key.DirectionDown -> dedState.moveNextRow()
                        Key.DirectionUp -> dedState.movePrevRow()
                        Key.A -> dedState.insert("A")
                    }
                }
                true
            }
    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code.copy(fontSize = 18.sp),
            colors = colors
        )
    }
}
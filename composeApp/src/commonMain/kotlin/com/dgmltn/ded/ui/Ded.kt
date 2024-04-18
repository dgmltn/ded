package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgmltn.ded.editor.StringBuilderEditor

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor
) {
    val dedState = rememberDedState(editor = StringBuilderEditor().apply {
        insert("Hello, world\nthis is a test\nLine #3")
        move(5)
    })

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code.copy(fontSize = 18.sp),
            colors = colors
        )
    }
}
package com.dgmltn.ded

import DedTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dgmltn.ded.ui.Ded
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    DedTheme {
        Ded(
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
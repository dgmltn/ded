package com.dgmltn.ded

import DedTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dgmltn.ded.ui.Ded
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    DedTheme {
        Ded(modifier = Modifier.fillMaxSize())
    }
}
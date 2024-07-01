package com.dgmltn.ded.sample

import DedSampleTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ded") {
        DedSampleTheme {
            App()
        }
    }
}

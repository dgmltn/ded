package com.dgmltn.ded.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ded") {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold {
                App()
            }
        }
    }
}

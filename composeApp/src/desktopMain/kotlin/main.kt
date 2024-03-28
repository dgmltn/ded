import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dgmltn.ded.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ded") {
        App()
    }
}
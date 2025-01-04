import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Arkanoid POC",
        state = WindowState(width = 800.dp, height = 800.dp,
            position = WindowPosition.Aligned(Alignment.Center)),
        resizable = false
    ) {
        ArkanoidGame()
    }
}






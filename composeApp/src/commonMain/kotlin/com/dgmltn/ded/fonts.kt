import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ded.composeapp.generated.resources.Res
import ded.composeapp.generated.resources.fira_code_regular
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalResourceApi::class)
@Composable
fun dedTypography(): FontFamily =
        FontFamily(
            Font(Res.font.fira_code_regular, FontWeight.Normal, FontStyle.Normal)
        )

//TODO all the others
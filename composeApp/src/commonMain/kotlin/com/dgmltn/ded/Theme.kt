import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ded.composeapp.generated.resources.Res
import ded.composeapp.generated.resources.fira_code_regular
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

internal val LocalColors: ProvidableCompositionLocal<DedColorScheme> =
    staticCompositionLocalOf { DedColorSchemeDark }
internal val LocalTypography: ProvidableCompositionLocal<DedTypography> =
    staticCompositionLocalOf { DedTypography }
internal val LocalShapes: ProvidableCompositionLocal<DedShapes> =
    staticCompositionLocalOf { DedShapes }
internal val LocalDefaults: ProvidableCompositionLocal<DedThemeDefaults> =
    staticCompositionLocalOf { DedThemeDefaults }

interface DedColorScheme {
    val isDarkTheme: Boolean

    /**
     * Material-inspired Colors
     */
    val primary: Color
    val onPrimary: Color
    val secondary: Color
    val onSecondary: Color
    val tertiary: Color
    val onTertiary: Color
    val background: Color
    val onBackground: Color
    val surface: Color
    val onSurface: Color
    val error: Color
    val onError: Color

    /**
     * Contextual Colors
     * These should be used in composables with custom
     * colors.
     */
    val editorCanvas: Color
    val editorCursor: Color
    val editorLineNumber: Color
    val editorText: Color
    //TODO: other editor colors

    //TODO: syntax hilight colors

    val m3: ColorScheme
        @Composable get() = if (this.isDarkTheme)
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                tertiary = tertiary,
                onTertiary = onTertiary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = error,
                errorContainer = error,
                onError = onError,
            )
        else
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                tertiary = tertiary,
                onTertiary = onTertiary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = error,
                errorContainer = error,
                onError = onError,
            )

}

object DedColorSchemeDark : DedColorScheme {
    private val base = darkColorScheme()

    override val isDarkTheme = true
    override val primary = base.primary
    override val onPrimary = base.onPrimary
    override val secondary = base.secondary
    override val onSecondary = base.onSecondary
    override val tertiary = base.tertiary
    override val onTertiary = base.onTertiary
    override val background = base.background
    override val onBackground = base.onBackground
    override val surface = base.surface
    override val onSurface = base.onSurface
    override val error = base.error
    override val onError = base.onError

    override val editorCanvas = base.background
    override val editorCursor = base.onBackground.copy(alpha = 0.5f)
    override val editorLineNumber = base.onBackground.copy(alpha = 0.5f)
    override val editorText = base.onBackground
}

object DedColorSchemeLight: DedColorScheme {
    private val base = lightColorScheme()

    override val isDarkTheme = false
    override val primary = base.primary
    override val onPrimary = base.onPrimary
    override val secondary = base.secondary
    override val onSecondary = base.onSecondary
    override val tertiary = base.tertiary
    override val onTertiary = base.onTertiary
    override val background = base.background
    override val onBackground = base.onBackground
    override val surface = base.surface
    override val onSurface = base.onSurface
    override val error = base.error
    override val onError = base.onError

    override val editorCanvas = base.background
    override val editorCursor = base.onBackground.copy(alpha = 0.5f)
    override val editorLineNumber = base.onBackground.copy(alpha = 0.5f)
    override val editorText = base.onBackground
}

object DedShapes {
    val m3 = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(10.dp),
    )

}

object DedTheme {
    val colors: DedColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: DedTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: DedShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    val defaults: DedThemeDefaults
        @Composable
        @ReadOnlyComposable
        get() = LocalDefaults.current
}

object DedThemeDefaults {
    val editor: DedColors
        @Composable
        get() = DedColors(
            canvas = DedTheme.colors.editorCanvas,
            cursor = DedTheme.colors.editorCursor,
            lineNumber = DedTheme.colors.editorLineNumber,
            text = DedTheme.colors.editorText,
        )
}

data class DedColors(
    val canvas: Color,
    val cursor: Color,
    val lineNumber: Color,
    val text: Color,
)

object DedTypography {
    @OptIn(ExperimentalResourceApi::class)
    val code: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = FontFamily(
                Font(Res.font.fira_code_regular, FontWeight.Normal, FontStyle.Normal)
            ),
            fontSize = 18.sp
        )

    internal val m3 = androidx.compose.material3.Typography()
}

@Composable
fun DedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DedColorSchemeDark else DedColorSchemeLight

    MaterialTheme(
        colorScheme = colorScheme.m3,
        typography = DedTypography.m3,
        shapes = DedShapes.m3
    ) {
        CompositionLocalProvider(
            LocalColors provides colorScheme,
            LocalContentColor provides colorScheme.onBackground,
            content = content
        )
    }
}

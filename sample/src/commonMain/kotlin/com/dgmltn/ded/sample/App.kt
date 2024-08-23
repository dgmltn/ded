package com.dgmltn.ded.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgmltn.ded.editor.LoggingEditor
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.parser.ThemeType
import com.dgmltn.ded.ui.Ded
import com.dgmltn.ded.ui.rememberDedState
import ded.sample.generated.resources.Res
import ded.sample.generated.resources.fira_code_regular
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    val editor = remember {
        LoggingEditor(StringBuilderEditor("""
                function hello() {
                  console.log("Hello, world!");
                  const x = 5;
                  var y = 10;
                  if (x == y) {
                    console.log("x equals y");
                  }
                }
            """.trimIndent(),
        ))
    }
    val state = rememberDedState(
        editor = editor,
        theme = ThemeType.MadeOfCode,
    )

    LaunchedEffect(editor) {
        while(true) {
            delay(10.seconds)
            editor.printStats()
        }
    }

    LaunchedEffect(state.value) {
        state.syncColors()
    }

    val textStyle = TextStyle(
        fontFamily = FontFamily(
            Font(Res.font.fira_code_regular, FontWeight.Normal, FontStyle.Normal)
        ),
        fontSize = 18.sp
    )

    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Ded(
            modifier = Modifier
                .fillMaxSize(),
            state = state,
            textStyle = textStyle,
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp),
            text = state.cursor.toString(),
        )
    }
}
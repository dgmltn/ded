package com.dgmltn.ded.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgmltn.ded.editor.LoggingEditor
import com.dgmltn.ded.editor.StringBuilderEditor
import com.dgmltn.ded.parser.LanguageType
import com.dgmltn.ded.parser.TextMateParser
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
        parser = TextMateParser(LanguageType.Javascript, ThemeType.MadeOfCode)
    )

    LaunchedEffect(editor) {
        while(true) {
            delay(10.seconds)
            editor.printStats()
        }
    }

    val textStyle = TextStyle(
        fontFamily = FontFamily(
            Font(Res.font.fira_code_regular, FontWeight.Normal, FontStyle.Normal)
        ),
        fontSize = 18.sp
    )

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Ded(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            textStyle = textStyle,
        )

        Row {
            TextButton(onClick = {state.undo()}) { Text("Undo") }
            TextButton(onClick = {state.redo()}) { Text("Redo") }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(5.dp),
                text = state.cursor.toString(),
            )
        }
    }
}
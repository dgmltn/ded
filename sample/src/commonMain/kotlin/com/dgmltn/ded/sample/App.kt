package com.dgmltn.ded.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dgmltn.ded.ui.Ded
import com.dgmltn.ded.ui.rememberDedState
import ded.sample.generated.resources.Res
import ded.sample.generated.resources.fira_code_regular
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    val state = rememberDedState()

    val textStyle = TextStyle(
        fontFamily = FontFamily(
            Font(Res.font.fira_code_regular, FontWeight.Normal, FontStyle.Normal)
        ),
        fontSize = 18.sp
    )

    LaunchedEffect(Unit) {
        state.run {
//            insert("const foo = \"bar\";")
            insert("""
                function hello() {
                  console.log("Hello, world!");
                  const x = 5;
                  var y = 10;
                  if (x == y) {
                    console.log("x equals y");
                  }
                }
            """.trimIndent())
            moveTo(5)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Ded(
            modifier = modifier
                .fillMaxSize(),
            state = state,
            textStyle = textStyle,
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp),
            text = state.cursorPos.toString(),
        )
    }
}
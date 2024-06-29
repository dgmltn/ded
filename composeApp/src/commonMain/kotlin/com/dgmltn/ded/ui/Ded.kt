package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    colors: DedColors = LocalDefaults.current.editor,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val textInputService = LocalTextInputService.current

    val focusRequester = remember { FocusRequester() }

    val dedState = rememberDedState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        dedState.run {
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

    LaunchedEffect(dedState.fullText) {
        dedState.buildHighlights()
    }

    //TODO: look at BasicTextField for keyboard api
    //BasicTextField("", {})

    val focusModifier = Modifier.dedFocusModifier(
        enabled = true,
        focusRequester = focusRequester,
        interactionSource = interactionSource,
        onFocused = {
            dedState.inputSession = textInputService?.startInput(
                value = TextFieldValue(""),
                imeOptions = ImeOptions(
                    autoCorrect = false,
//                    keyboardType = KeyboardType.Ascii, // Backspace doesn't work right with .Ascii
                    keyboardType = KeyboardType.Password,
                ),
                onEditCommand = { editCommands ->
                    Logger.e { "onEditCommand: $editCommands" }
                    editCommands.forEach {
                        if (it is CommitTextCommand) {
                            dedState.insert(it.text)
                        }
                    }
                },
                onImeActionPerformed = { action ->
                    Logger.e { "onImeActionPerformed: $action" }
                },
            )
        },
        onUnfocused = {
            dedState.inputSession?.let {
                textInputService?.stopInput(it)
            }
            dedState.inputSession = null
         }
    )

    val gestureModifier = Modifier.dedGestureModifier(dedState)

    Box(
        modifier = modifier
            .background(colors.canvas)
            .padding(5.dp)
            .then(focusModifier)
            .dedKeyEvent(dedState)
            .then(gestureModifier)

    ) {
        DedGrid(
            state = dedState,
            modifier = Modifier.fillMaxSize(),
            textStyle = DedTheme.typography.code,
            colors = colors
        )

        Text(
            modifier = Modifier.align(Alignment.BottomEnd),
            text = dedState.cursorPos.toString(),
        )
    }
}
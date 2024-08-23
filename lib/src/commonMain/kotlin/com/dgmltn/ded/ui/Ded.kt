package com.dgmltn.ded.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.parser.ThemeType

@Composable
fun Ded(
    modifier: Modifier = Modifier,
    state: DedState = rememberDedState(),
    textStyle: TextStyle = LocalTextStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val textInputService = LocalTextInputService.current

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    //TODO: look at BasicTextField for keyboard api
    //BasicTextField("", {})
//    TextField()

    val focusModifier = Modifier.dedFocusModifier(
        enabled = true,
        focusRequester = focusRequester,
        interactionSource = interactionSource,
        onFocused = {
            state.inputSession = textInputService?.startInput(
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
                            state.insert(it.text)
                        }
                    }
                },
                onImeActionPerformed = { action ->
                    Logger.e { "onImeActionPerformed: $action" }
                },
            )
        },
        onUnfocused = {
            state.inputSession?.let {
                textInputService?.stopInput(it)
            }
            state.inputSession = null
         }
    )

    val gestureModifier = Modifier.dedGestureModifier(state)

    Box(
        modifier = modifier
            .background(state.theme.defaultBg)
            .clipToBounds()
            .padding(5.dp)
            .then(focusModifier)
            .dedKeyEvent(state)
            .then(gestureModifier)

    ) {
        DedGrid(
            state = state,
            modifier = Modifier.fillMaxSize(),
            textStyle = textStyle,
        )
    }
}
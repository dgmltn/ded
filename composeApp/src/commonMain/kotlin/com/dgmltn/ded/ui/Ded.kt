package com.dgmltn.ded.ui

import DedColors
import DedTheme
import LocalDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.dgmltn.ded.div
import com.dgmltn.ded.editor.RowCol
import com.dgmltn.ded.editor.toRowCol
import com.dgmltn.ded.toInt

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
    BasicTextField("", {})

    val focusModifier = Modifier.dedFocusModifier(
        enabled = true,
        focusRequester = focusRequester,
        interactionSource = interactionSource,
        onFocusChanged = { focusState ->
            Logger.e { "onFocusChanged: $focusState" }
            if (focusState.isFocused) {
                dedState.inputSession = textInputService?.startInput(
                    value = TextFieldValue(""),
                    imeOptions = ImeOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Ascii
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
            }
            else {
                dedState.inputSession?.let {
                    textInputService?.stopInput(it)
                }
                dedState.inputSession = null
            }
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

internal fun Modifier.dedFocusModifier(
    enabled: Boolean,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource?,
    onFocusChanged: (FocusState) -> Unit
) = this
    .focusRequester(focusRequester)
    .onFocusChanged(onFocusChanged)
    .focusable(interactionSource = interactionSource, enabled = enabled)

@Composable
private fun Modifier.dedGestureModifier(
    dedState: DedState,
) = this
        .detectSelectGestures(dedState)
        .detectScrollGestures(dedState)

@Composable
fun Modifier.detectScrollGestures(
    dedState: DedState,
) = this
    .scrollable(
        orientation = Orientation.Vertical,
        // Scrollable state: describes how to consume
        // scrolling delta and update offset
        state = rememberScrollableState { delta ->
            val min = 0f
            val max = dedState.maxWindowYScrollPx.toFloat()
            val current = dedState.windowYScrollPx
            val minDelta = min - current
            val maxDelta = max - current
            val clipped = (-delta).coerceIn(minDelta, maxDelta)
            dedState.windowYScrollPx += clipped
            -clipped
        }
    )

@Composable
fun Modifier.detectSelectGestures(
    dedState: DedState,
) = this
    .pointerInput(Unit) {
        detectTapGestures { offset ->
            val col = (offset.x / dedState.cellSizePx.width).toInt() - dedState.cellOffset.x
            val row = ((offset.y / dedState.cellSizePx.height).toInt() - dedState.cellOffset.y)
                .coerceAtMost(dedState.rowCount - 1)
                .coerceAtLeast(0)
            dedState.moveTo(RowCol(row, col))
        }
    }
    .pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                val cellOffset = offset.div(dedState.cellSizePx).toInt() - dedState.cellOffset
                dedState.moveTo(cellOffset.toRowCol())
            },
            onDrag = { change, _ ->
                val cellOffset =
                    change.position.div(dedState.cellSizePx).toInt() - dedState.cellOffset
                dedState.withSelection { dedState.moveTo(cellOffset.toRowCol()) }
                change.consume()
            },
        )
    }
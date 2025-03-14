package com.dgmltn.ded.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged


fun Modifier.dedFocusModifier(
    enabled: Boolean,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource?,
    onFocused: () -> Unit,
    onUnfocused: () -> Unit,
) = this
    .focusRequester(focusRequester)
    .onFocusChanged {
        if (it.isFocused) onFocused()
        else onUnfocused()
    }
    .focusable(interactionSource = interactionSource, enabled = enabled)

@Composable
expect fun Modifier.dedGestureModifier(dedState: DedState, focusRequester: FocusRequester): Modifier

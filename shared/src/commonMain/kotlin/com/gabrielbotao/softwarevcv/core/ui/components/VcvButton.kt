package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

private const val ButtonPressScale = 0.96f

/**
 * Button hierarchy (reused Production Line rule, [[VCV Design-System]] §9): only the **single** primary
 * action is filled ([VcvButton]); every other action is **outlined** ([VcvOutlinedButton]); text links
 * ([VcvTextLink]) are for in-text affordances only, never a standalone action. The filled/outlined
 * buttons **dip slightly on press** for tactile feedback (VCV-32).
 */
@Composable
fun VcvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        interactionSource = interaction,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun VcvOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        interactionSource = interaction,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun VcvTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, style = MaterialTheme.typography.labelLarge, textDecoration = TextDecoration.Underline)
    }
}

/** Dips the element to [ButtonPressScale] while [interaction] is pressed — a light tactile response. */
@Composable
private fun Modifier.pressScale(interaction: MutableInteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) ButtonPressScale else 1f,
        animationSpec = tween(Vcv.motion.hoverFadeMillis),
        label = "button-press",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

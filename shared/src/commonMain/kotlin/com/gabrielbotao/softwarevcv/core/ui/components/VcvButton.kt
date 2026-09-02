package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration

/**
 * Button hierarchy (reused Production Line rule, [[VCV Design-System]] §9): only the **single** primary
 * action is filled ([VcvButton]); every other action is **outlined** ([VcvOutlinedButton]); text links
 * ([VcvTextLink]) are for in-text affordances only, never a standalone action.
 */
@Composable
fun VcvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled, shape = MaterialTheme.shapes.small) {
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
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = MaterialTheme.shapes.small) {
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

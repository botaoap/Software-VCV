package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * A navigation entry: a label, its action, and an optional numeric [badge] (e.g. the cart count).
 * Presentational — VCV-4 wires these to routes.
 */
data class VcvNavItem(val label: String, val badge: Int? = null, val onClick: () -> Unit)

/**
 * Responsive top bar: logo + inline links on expanded, a "Menu" dropdown on compact. An item's [badge]
 * renders a small count pill that **pulses when the count changes** — the add-to-cart confirmation
 * (VCV-32). Purely presentational (VCV-4 owns the nav host + routes). See [[VCV Screens-and-UX]] §0.
 */
@Composable
fun VcvTopNav(
    items: List<VcvNavItem>,
    modifier: Modifier = Modifier,
    onLogoClick: () -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        val compact = WindowWidthClass.of(maxWidth) == WindowWidthClass.COMPACT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Vcv.spacing.lg, vertical = Vcv.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "VCV",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onLogoClick).padding(vertical = Vcv.spacing.xs),
            )
            if (compact) {
                CompactMenu(items)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item -> NavLink(item) }
                }
            }
        }
    }
}

@Composable
private fun NavLink(item: VcvNavItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.xs),
        modifier = Modifier.clickable(onClick = item.onClick).padding(vertical = Vcv.spacing.xs),
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        item.badge?.let { CountBadge(it) }
    }
}

/** Small count pill that pulses whenever [count] changes — the cart's add confirmation. */
@Composable
private fun CountBadge(count: Int) {
    val scale = remember { Animatable(1f) }
    val pulse = Vcv.motion.hoverFadeMillis
    LaunchedEffect(count) {
        scale.animateTo(1.35f, tween(pulse))
        scale.animateTo(1f, tween(pulse))
    }
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .sizeIn(minWidth = Vcv.spacing.lg, minHeight = Vcv.spacing.lg)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .padding(horizontal = Vcv.spacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CompactMenu(items: List<VcvNavItem>) {
    var open by remember { mutableStateOf(false) }
    Box {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clickable { open = true }
                .padding(vertical = Vcv.spacing.xs, horizontal = Vcv.spacing.sm),
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            items.forEach { item ->
                val label = item.badge?.let { "${item.label} ($it)" } ?: item.label
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        open = false
                        item.onClick()
                    },
                )
            }
        }
    }
}

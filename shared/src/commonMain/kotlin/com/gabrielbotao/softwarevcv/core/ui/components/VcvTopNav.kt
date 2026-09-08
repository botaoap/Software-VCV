package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** A navigation entry: a label and its action. Presentational — VCV-4 wires these to routes. */
data class VcvNavItem(val label: String, val onClick: () -> Unit)

/**
 * Responsive top bar: logo + inline links on expanded, a "Menu" dropdown on compact. Purely
 * presentational (VCV-4 owns the nav host + routes). See [[VCV Screens-and-UX]] §0.
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
                    items.forEach { item ->
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable(onClick = item.onClick).padding(vertical = Vcv.spacing.xs),
                        )
                    }
                }
            }
        }
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
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        open = false
                        item.onClick()
                    },
                )
            }
        }
    }
}

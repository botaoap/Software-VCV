package com.gabrielbotao.softwarevcv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.core.ui.theme.VcvTheme

/**
 * Root composable, hosted by every platform entrypoint. VCV-2 wraps it in [VcvTheme] and renders a
 * minimal token-only placeholder (the real responsive shell arrives in VCV-4). Uses **only** design
 * tokens — no raw colors/`dp` — per [[VCV Design-System]] §1.
 */
@Composable
@Preview
fun App() {
    VcvTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(Vcv.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "VCV",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(Vcv.spacing.sm))
            Text(
                text = "Veste Com Você",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Vcv.spacing.xs))
            Text(
                text = "Gaspar · Vale do Itajaí",
                style = MaterialTheme.typography.labelMedium,
                color = Vcv.colors.muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

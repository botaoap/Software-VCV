package com.gabrielbotao.softwarevcv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Root composable, hosted by every platform entrypoint. VCV-1 ships a minimal placeholder; the real
 * responsive shell (top nav + footer + nav host) arrives in VCV-4. See [[MVVM-Multiplatform]].
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("VCV — Veste Com Você", style = MaterialTheme.typography.titleLarge)
        }
    }
}

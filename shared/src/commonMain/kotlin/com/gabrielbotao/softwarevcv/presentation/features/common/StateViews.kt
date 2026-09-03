package com.gabrielbotao.softwarevcv.presentation.features.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** Shared loading / empty / error states, reused across feature screens. See [[VCV Screens-and-UX]] §7. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(Vcv.spacing.xl), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(Vcv.spacing.xl), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = Vcv.colors.muted)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.muted)
        VcvButton(text = "Tentar de novo", onClick = onRetry)
    }
}

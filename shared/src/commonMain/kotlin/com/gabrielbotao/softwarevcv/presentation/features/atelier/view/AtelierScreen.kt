package com.gabrielbotao.softwarevcv.presentation.features.atelier.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.AtelierSection
import com.gabrielbotao.softwarevcv.presentation.features.atelier.state.AtelierUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingState
import org.koin.compose.viewmodel.koinViewModel

// Cap the editorial column so images + text don't sprawl across a wide desktop (VCV-16).
private val AtelierMaxWidth = 960.dp

/** Atelier / Sobre — the origin story, photo-led editorial sections. See [[VCV Screens-and-UX]] §5. */
@Composable
fun AtelierScreen(viewModel: AtelierViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error
    val content = state.content
    when {
        state.isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = { viewModel.onEvent(AtelierUiEvent.Retry) })
        content != null -> AtelierContentView(content)
        else -> EmptyState(message = "Em breve.")
    }
}

@Composable
private fun AtelierContentView(content: AtelierContent) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val stacked = WindowWidthClass.of(maxWidth) == WindowWidthClass.COMPACT
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Centered editorial column, capped width.
            Column(
                modifier = Modifier
                    .widthIn(max = AtelierMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = Vcv.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xl),
            ) {
                Text(
                    text = content.headline,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Vcv.spacing.xl),
                )
                content.sections.forEachIndexed { index, section ->
                    AtelierSectionView(section, imageFirst = index % 2 == 0, stacked = stacked)
                }
            }
            // Full-bleed footer at the end.
            AppFooter(Modifier.padding(top = Vcv.spacing.xl))
        }
    }
}

@Composable
private fun AtelierSectionView(section: AtelierSection, imageFirst: Boolean, stacked: Boolean) {
    val image: @Composable (Modifier) -> Unit = { m ->
        section.image?.let { img ->
            RemoteImage(
                url = img.url,
                contentDescription = img.alt,
                aspectRatio = img.aspectRatio,
                modifier = m,
            )
        }
    }
    val text: @Composable (Modifier) -> Unit = { m ->
        Column(modifier = m, verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
            SectionHeader(title = section.title)
            Text(section.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
            image(Modifier.fillMaxWidth())
            text(Modifier.fillMaxWidth())
        }
    } else {
        // Side-by-side, alternating which side the photo sits on for editorial rhythm.
        Row(
            horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (imageFirst) {
                image(Modifier.weight(1f))
                text(Modifier.weight(1f))
            } else {
                text(Modifier.weight(1f))
                image(Modifier.weight(1f))
            }
        }
    }
}

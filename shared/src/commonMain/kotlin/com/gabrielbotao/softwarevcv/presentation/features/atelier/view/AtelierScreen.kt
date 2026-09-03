package com.gabrielbotao.softwarevcv.presentation.features.atelier.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.AtelierSection
import com.gabrielbotao.softwarevcv.presentation.features.atelier.state.AtelierUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingState
import org.koin.compose.viewmodel.koinViewModel

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
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xl),
    ) {
        Text(
            text = content.headline,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(Vcv.spacing.lg),
        )
        content.sections.forEach { section -> AtelierSectionView(section) }
        Spacer(Modifier.height(Vcv.spacing.md))
    }
}

@Composable
private fun AtelierSectionView(section: AtelierSection) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        section.image?.let { image ->
            RemoteImage(
                url = image.url,
                contentDescription = image.alt,
                aspectRatio = 3f / 2f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
        ) {
            SectionHeader(title = section.title)
            Text(section.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

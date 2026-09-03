package com.gabrielbotao.softwarevcv.presentation.features.collections.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.ProductGrid
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.presentation.features.collections.state.CollectionsUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel.CollectionsViewModel
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingGrid
import org.koin.compose.viewmodel.koinViewModel

/** Collections list (`/colecoes`) — cover cards that open a collection. See [[VCV Screens-and-UX]] §2. */
@Composable
fun CollectionsScreen(
    onCollection: (String) -> Unit,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error
    when {
        state.isLoading -> LoadingGrid()
        error != null -> ErrorState(message = error, onRetry = { viewModel.onEvent(CollectionsUiEvent.Retry) })
        state.collections.isEmpty() -> EmptyState(message = "Em breve novas coleções.")
        else -> ProductGrid(
            items = state.collections,
            modifier = Modifier.fillMaxSize(),
            header = { SectionHeader(title = "Coleções", modifier = Modifier.padding(bottom = Vcv.spacing.sm)) },
        ) { collection ->
            CollectionCover(collection = collection, onClick = { onCollection(collection.slug) })
        }
    }
}

@Composable
private fun CollectionCover(collection: Collection, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        RemoteImage(
            url = collection.cover.url,
            contentDescription = collection.title,
            aspectRatio = 3f / 4f,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Vcv.spacing.sm))
        Text(collection.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        collection.subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = Vcv.colors.muted)
        }
    }
}

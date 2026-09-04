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
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.ProductGrid
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.strings.Strings
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.collections.state.CollectionsUiState
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingGrid

/**
 * Collections list (`/colecoes`) — cover cards that open a collection. Stateless: the route owns the
 * ViewModel and passes [state] + callbacks (VCV-28). See [[VCV Screens-and-UX]] §2.
 */
@Composable
fun CollectionsScreen(
    state: CollectionsUiState,
    onRetry: () -> Unit,
    onCollection: (String) -> Unit,
) {
    when {
        state.isLoading -> LoadingGrid()
        state.error != null -> ErrorState(message = state.error, onRetry = onRetry)
        state.collections.isEmpty() -> EmptyState(message = Strings.Collections.empty)
        else -> ProductGrid(
            items = state.collections,
            modifier = Modifier.fillMaxSize(),
            header = { SectionHeader(title = Strings.Collections.title, modifier = Modifier.padding(bottom = Vcv.spacing.sm)) },
            footer = { AppFooter() },
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

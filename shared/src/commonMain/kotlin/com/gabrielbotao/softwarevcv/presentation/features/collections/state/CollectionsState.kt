package com.gabrielbotao.softwarevcv.presentation.features.collections.state

import com.gabrielbotao.softwarevcv.domain.model.Collection

/** UI state for the collections list (`/colecoes`). */
data class CollectionsUiState(
    val isLoading: Boolean = true,
    val collections: List<Collection> = emptyList(),
    val error: String? = null,
)

sealed interface CollectionsUiEvent {
    data object Retry : CollectionsUiEvent
}

package com.gabrielbotao.softwarevcv.presentation.features.catalog.state

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.Product

/** UI state for the catalog / collection-detail grid. `collection` is null for the all-products `/catalogo`. */
data class CatalogUiState(
    val isLoading: Boolean = true,
    val collection: Collection? = null,
    val products: List<Product> = emptyList(),
    val error: String? = null,
)

sealed interface CatalogUiEvent {
    data object Retry : CatalogUiEvent
}

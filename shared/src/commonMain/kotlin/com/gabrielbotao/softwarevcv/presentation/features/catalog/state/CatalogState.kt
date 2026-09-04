package com.gabrielbotao.softwarevcv.presentation.features.catalog.state

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.Product

/** Sort options for the catalog grid (VCV-24). `FEATURED` keeps the content order. */
enum class CatalogSort(val label: String) {
    FEATURED("Novidades"),
    PRICE_ASC("Menor preço"),
    PRICE_DESC("Maior preço"),
    NAME_ASC("Nome A–Z"),
}

/** UI state for the catalog / collection-detail grid. `collection` is null for the all-products `/catalogo`. */
data class CatalogUiState(
    val isLoading: Boolean = true,
    val collection: Collection? = null,
    val products: List<Product> = emptyList(),
    val sort: CatalogSort = CatalogSort.FEATURED,
    val error: String? = null,
)

sealed interface CatalogUiEvent {
    data object Retry : CatalogUiEvent
    data class SortChanged(val sort: CatalogSort) : CatalogUiEvent
}

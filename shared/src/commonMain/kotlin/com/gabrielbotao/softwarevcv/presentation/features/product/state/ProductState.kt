package com.gabrielbotao.softwarevcv.presentation.features.product.state

import com.gabrielbotao.softwarevcv.domain.model.Product

/** UI state for the product detail page. `product == null` after loading ⇒ not found (404). */
data class ProductUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val error: String? = null,
)

sealed interface ProductUiEvent {
    data object Retry : ProductUiEvent
}

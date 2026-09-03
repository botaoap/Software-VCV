package com.gabrielbotao.softwarevcv.presentation.features.home.state

import com.gabrielbotao.softwarevcv.domain.model.Product

/** UI state for the Home page. Navigation is via lambdas (no one-shot effects needed here). */
data class HomeUiState(
    val isLoading: Boolean = true,
    val featured: List<Product> = emptyList(),
    val bestSeller: Product? = null,
    val error: String? = null,
)

sealed interface HomeUiEvent {
    data object Retry : HomeUiEvent
}

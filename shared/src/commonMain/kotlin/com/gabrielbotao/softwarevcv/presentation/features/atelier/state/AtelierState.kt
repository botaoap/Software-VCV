package com.gabrielbotao.softwarevcv.presentation.features.atelier.state

import com.gabrielbotao.softwarevcv.domain.model.AtelierContent

/** UI state for the Atelier / Sobre page. */
data class AtelierUiState(
    val isLoading: Boolean = true,
    val content: AtelierContent? = null,
    val error: String? = null,
)

sealed interface AtelierUiEvent {
    data object Retry : AtelierUiEvent
}

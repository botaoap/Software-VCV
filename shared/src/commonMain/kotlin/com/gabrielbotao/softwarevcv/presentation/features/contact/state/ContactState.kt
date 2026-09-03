package com.gabrielbotao.softwarevcv.presentation.features.contact.state

import com.gabrielbotao.softwarevcv.domain.model.ContactContent

/** UI state for the Contato page. */
data class ContactUiState(
    val isLoading: Boolean = true,
    val content: ContactContent? = null,
    val error: String? = null,
)

sealed interface ContactUiEvent {
    data object Retry : ContactUiEvent
}

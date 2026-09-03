package com.gabrielbotao.softwarevcv.presentation.features.contact.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import com.gabrielbotao.softwarevcv.presentation.features.contact.state.ContactUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.contact.state.ContactUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the Contato / where-to-buy content. */
class ContactViewModel(private val getContact: GetContactUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: ContactUiEvent) = when (event) {
        ContactUiEvent.Retry -> load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getContact()
                .onSuccess { content -> _uiState.update { it.copy(isLoading = false, content = content) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") } }
        }
    }
}

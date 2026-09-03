package com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetAtelierUseCase
import com.gabrielbotao.softwarevcv.presentation.features.atelier.state.AtelierUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.atelier.state.AtelierUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the Atelier / Sobre story content. */
class AtelierViewModel(private val getAtelier: GetAtelierUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(AtelierUiState())
    val uiState: StateFlow<AtelierUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: AtelierUiEvent) = when (event) {
        AtelierUiEvent.Retry -> load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAtelier()
                .onSuccess { content -> _uiState.update { it.copy(isLoading = false, content = content) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") } }
        }
    }
}

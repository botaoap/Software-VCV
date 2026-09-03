package com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.collections.state.CollectionsUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.collections.state.CollectionsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the collections for the `/colecoes` list. */
class CollectionsViewModel(private val getCollections: GetCollectionsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: CollectionsUiEvent) = when (event) {
        CollectionsUiEvent.Retry -> load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getCollections()
                .onSuccess { collections -> _uiState.update { it.copy(isLoading = false, collections = collections) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") } }
        }
    }
}

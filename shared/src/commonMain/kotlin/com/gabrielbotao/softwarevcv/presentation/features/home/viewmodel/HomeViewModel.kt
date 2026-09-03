package com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge
import com.gabrielbotao.softwarevcv.domain.usecase.GetFeaturedProductsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the featured products (and picks the best-seller) for the Home page. See [[MVVM-Multiplatform]]. */
class HomeViewModel(private val getFeatured: GetFeaturedProductsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: HomeUiEvent) = when (event) {
        HomeUiEvent.Retry -> load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getFeatured()
                .onSuccess { products ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            featured = products,
                            bestSeller = products.firstOrNull { p -> ProductBadge.BEST_SELLER in p.badges },
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") }
                }
        }
    }
}

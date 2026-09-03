package com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs both `/catalogo` (all products, `slug == null`) and `/colecao/{slug}` (a collection's products +
 * its header). The screen calls [load] with the route's slug; re-navigating to a new slug reloads.
 */
class CatalogViewModel(
    private val getProducts: GetProductsUseCase,
    private val getCollection: GetCollectionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var currentSlug: String? = null
    private var loaded = false

    /** Load (or reload if the slug changed). Called from the screen for the current route. */
    fun load(slug: String?) {
        if (loaded && slug == currentSlug) return
        currentSlug = slug
        loaded = true
        refresh()
    }

    fun onEvent(event: CatalogUiEvent) = when (event) {
        CatalogUiEvent.Retry -> refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val slug = currentSlug
            getProducts(slug)
                .onSuccess { products ->
                    val collection = slug?.let { getCollection(it).getOrNull() }
                    _uiState.update { it.copy(isLoading = false, products = products, collection = collection) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar") }
                }
        }
    }
}

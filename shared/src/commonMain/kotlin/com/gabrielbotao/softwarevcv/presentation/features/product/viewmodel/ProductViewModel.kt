package com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductUseCase
import com.gabrielbotao.softwarevcv.presentation.features.common.toUserMessage
import com.gabrielbotao.softwarevcv.presentation.features.product.state.ProductUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.product.state.ProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads one product by id for `/produto/{id}`. The screen calls [load] with the route id. */
class ProductViewModel(private val getProduct: GetProductUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var currentId: String? = null
    private var loaded = false

    fun load(id: String) {
        if (loaded && id == currentId) return
        currentId = id
        loaded = true
        refresh()
    }

    fun onEvent(event: ProductUiEvent) = when (event) {
        ProductUiEvent.Retry -> refresh()
    }

    private fun refresh() {
        val id = currentId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getProduct(id)
                .onSuccess { product -> _uiState.update { it.copy(isLoading = false, product = product) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, product = null, error = e.toUserMessage("Produto não encontrado.")) }
                }
        }
    }
}

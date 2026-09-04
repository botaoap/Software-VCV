package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.product.view.ProductScreen
import com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel.ProductViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Product detail destination wiring (`/produto/{id}`) — route owns the ViewModel + the `load(id)`
 * effect; [ProductScreen] is stateless ([[VCV-28]]).
 */
fun EntryProviderScope<AppRoute>.productEntry(navigator: Navigator) {
    entry<AppRoute.Product> { key ->
        val viewModel: ProductViewModel = koinViewModel()
        LaunchedEffect(key.id) { viewModel.load(key.id) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ProductScreen(
            state = state,
            onAddToCart = viewModel::addToCart,
            onCatalog = { navigator.navigate(AppRoute.Catalog) },
        )
    }
}

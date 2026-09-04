package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.catalog.view.CatalogScreen
import com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel.CatalogViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/** Catalog (all products) destination wiring — route owns the ViewModel + `load` ([[VCV-28]]). */
fun EntryProviderScope<AppRoute>.catalogEntry(navigator: Navigator) {
    entry<AppRoute.Catalog> {
        val viewModel: CatalogViewModel = koinViewModel()
        LaunchedEffect(Unit) { viewModel.load(null) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        CatalogScreen(
            state = state,
            onRetry = { viewModel.onEvent(CatalogUiEvent.Retry) },
            onSort = { viewModel.onEvent(CatalogUiEvent.SortChanged(it)) },
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
        )
    }
}

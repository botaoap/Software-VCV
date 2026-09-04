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

/** Single-collection destination wiring (`/colecao/{slug}`) — reuses the catalog grid ([[VCV-28]]). */
fun EntryProviderScope<AppRoute>.collectionEntry(navigator: Navigator) {
    entry<AppRoute.Collection> { key ->
        val viewModel: CatalogViewModel = koinViewModel()
        LaunchedEffect(key.slug) { viewModel.load(key.slug) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        CatalogScreen(
            state = state,
            onRetry = { viewModel.onEvent(CatalogUiEvent.Retry) },
            onSort = { viewModel.onEvent(CatalogUiEvent.SortChanged(it)) },
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
        )
    }
}

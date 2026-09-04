package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.collections.state.CollectionsUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.collections.view.CollectionsScreen
import com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel.CollectionsViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/** Collections list destination wiring (`/colecoes`) — route owns the ViewModel ([[VCV-28]]). */
fun EntryProviderScope<AppRoute>.collectionsEntry(navigator: Navigator) {
    entry<AppRoute.Collections> {
        val viewModel: CollectionsViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        CollectionsScreen(
            state = state,
            onRetry = { viewModel.onEvent(CollectionsUiEvent.Retry) },
            onCollection = { slug -> navigator.navigate(AppRoute.Collection(slug)) },
        )
    }
}

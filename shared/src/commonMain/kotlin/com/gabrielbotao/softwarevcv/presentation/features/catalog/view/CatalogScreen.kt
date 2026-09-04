package com.gabrielbotao.softwarevcv.presentation.features.catalog.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.ProductCard
import com.gabrielbotao.softwarevcv.core.ui.components.ProductGrid
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvFooter
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogSort
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel.CatalogViewModel
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingGrid
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeColor
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeLabel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Catalog / collection-detail grid. [slug] null = all products (`/catalogo`); a slug = that collection
 * (`/colecao/{slug}`) with its story header. Nav via [onProduct]. See [[VCV Screens-and-UX]] §3.
 */
@Composable
fun CatalogScreen(
    slug: String?,
    onProduct: (String) -> Unit,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    LaunchedEffect(slug) { viewModel.load(slug) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error
    when {
        state.isLoading -> LoadingGrid()
        error != null -> ErrorState(message = error, onRetry = { viewModel.onEvent(CatalogUiEvent.Retry) })
        state.products.isEmpty() -> EmptyState(message = "Em breve novas peças.")
        else -> ProductGrid(
            items = state.products,
            modifier = Modifier.fillMaxSize(),
            header = {
                CatalogHeader(
                    collection = state.collection,
                    sort = state.sort,
                    onSort = { viewModel.onEvent(CatalogUiEvent.SortChanged(it)) },
                )
            },
            footer = { VcvFooter() },
        ) { product ->
            val badge = product.badges.firstOrNull()
            ProductCard(
                name = product.name,
                priceCents = product.price.amountCents,
                imageUrl = product.cover?.url,
                badgeText = badge?.let(::productBadgeLabel),
                badgeColor = badge?.let { productBadgeColor(it) } ?: MaterialTheme.colorScheme.primary,
                onClick = { onProduct(product.id) },
            )
        }
    }
}

@Composable
private fun CatalogHeader(collection: Collection?, sort: CatalogSort, onSort: (CatalogSort) -> Unit) {
    Column(
        modifier = Modifier.padding(bottom = Vcv.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
    ) {
        SectionHeader(
            title = collection?.title ?: "Catálogo",
            subtitle = collection?.subtitle ?: "Todas as peças",
        )
        collection?.story?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.muted)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SortControl(sort = sort, onSort = onSort)
        }
    }
}

/** A small "Ordenar por: X" menu for the catalog grid (VCV-24). */
@Composable
private fun SortControl(sort: CatalogSort, onSort: (CatalogSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = "Ordenar por: ${sort.label}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(Vcv.spacing.xs),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CatalogSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSort(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

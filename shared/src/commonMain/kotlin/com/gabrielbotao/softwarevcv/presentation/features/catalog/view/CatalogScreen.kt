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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.ProductCard
import com.gabrielbotao.softwarevcv.core.ui.components.ProductGrid
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.strings.Strings
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogSort
import com.gabrielbotao.softwarevcv.presentation.features.catalog.state.CatalogUiState
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingGrid
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeColor
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeLabel

/**
 * Catalog / collection-detail grid. Stateless: the route owns the ViewModel + the `load(slug)` effect,
 * passing [state] + callbacks (VCV-28). See [[VCV Screens-and-UX]] §3.
 */
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onRetry: () -> Unit,
    onSort: (CatalogSort) -> Unit,
    onProduct: (String) -> Unit,
) {
    when {
        state.isLoading -> LoadingGrid()
        state.error != null -> ErrorState(message = state.error, onRetry = onRetry)
        state.products.isEmpty() -> EmptyState(message = Strings.Catalog.empty)
        else -> ProductGrid(
            items = state.products,
            modifier = Modifier.fillMaxSize(),
            header = { CatalogHeader(state.collection, state.sort, onSort) },
            footer = { AppFooter() },
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
            title = collection?.title ?: Strings.Catalog.title,
            subtitle = collection?.subtitle ?: Strings.Catalog.subtitle,
        )
        collection?.story?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.muted)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SortControl(sort = sort, onSort = onSort)
        }
    }
}

@Composable
private fun SortControl(sort: CatalogSort, onSort: (CatalogSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = Strings.Catalog.sortPrefix + sort.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { expanded = true }.padding(Vcv.spacing.xs),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CatalogSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onSort(option); expanded = false },
                )
            }
        }
    }
}

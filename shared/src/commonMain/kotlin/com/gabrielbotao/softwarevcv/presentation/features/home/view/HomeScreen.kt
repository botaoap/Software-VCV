package com.gabrielbotao.softwarevcv.presentation.features.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.ProductCard
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiState
import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

private val CardWidth = 240.dp

/** Home page — the brand's first impression. Nav is via lambdas; data via [HomeViewModel]. §1. */
@Composable
fun HomeScreen(
    onProduct: (String) -> Unit,
    onCatalog: () -> Unit,
    onAtelier: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onRetry = { viewModel.onEvent(HomeUiEvent.Retry) },
        onProduct = onProduct,
        onCatalog = onCatalog,
        onAtelier = onAtelier,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    onProduct: (String) -> Unit,
    onCatalog: () -> Unit,
    onAtelier: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Hero(imageUrl = state.bestSeller?.cover?.url, onCatalog = onCatalog)
        when {
            state.isLoading -> LoadingSection()
            state.error != null -> ErrorSection(message = state.error, onRetry = onRetry)
            else -> FeaturedSection(products = state.featured, onProduct = onProduct)
        }
        AtelierTeaser(onAtelier = onAtelier)
        Spacer(Modifier.height(Vcv.spacing.xl))
    }
}

@Composable
private fun Hero(imageUrl: String?, onCatalog: () -> Unit) {
    Column {
        if (imageUrl != null) {
            RemoteImage(
                url = imageUrl,
                contentDescription = "VCV — Veste Com Você",
                aspectRatio = 4f / 5f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.padding(Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
        ) {
            Text("Veste Com Você", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(
                "De Gaspar, Vale do Itajaí — poucas peças, bem feitas.",
                style = MaterialTheme.typography.bodyLarge,
                color = Vcv.colors.muted,
            )
            VcvButton(text = "Ver catálogo", onClick = onCatalog)
        }
    }
}

@Composable
private fun FeaturedSection(products: List<Product>, onProduct: (String) -> Unit) {
    if (products.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        SectionHeader(title = "Peças em destaque", modifier = Modifier.padding(horizontal = Vcv.spacing.lg))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Vcv.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
        ) {
            items(products) { product ->
                ProductCard(
                    name = product.name,
                    priceCents = product.price.amountCents,
                    imageUrl = product.cover?.url,
                    badgeText = product.badges.firstOrNull()?.let(::badgeLabel),
                    badgeColor = Vcv.colors.badgeBestSeller,
                    onClick = { onProduct(product.id) },
                    modifier = Modifier.width(CardWidth),
                )
            }
        }
    }
}

@Composable
private fun AtelierTeaser(onAtelier: () -> Unit) {
    Column(
        modifier = Modifier.padding(Vcv.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
    ) {
        SectionHeader(title = "Do ateliê", subtitle = "Ateliê próprio em Gaspar · produção rolo a rolo")
        VcvOutlinedButton(text = "Conheça o ateliê", onClick = onAtelier)
    }
}

@Composable
private fun LoadingSection() {
    Box(Modifier.fillMaxWidth().padding(Vcv.spacing.xl), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.muted)
        VcvButton(text = "Tentar de novo", onClick = onRetry)
    }
}

private fun badgeLabel(badge: ProductBadge): String = when (badge) {
    ProductBadge.BEST_SELLER -> "Best-seller"
    ProductBadge.NEW_IN -> "Novo"
    ProductBadge.LAST_UNITS -> "Últimas peças"
    ProductBadge.ATELIER_PICK -> "Escolha do ateliê"
}

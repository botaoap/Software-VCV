package com.gabrielbotao.softwarevcv.presentation.features.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.components.ImageCarousel
import com.gabrielbotao.softwarevcv.core.ui.components.ProductCard
import com.gabrielbotao.softwarevcv.core.ui.components.RevealOnAppear
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.TrustBadge
import com.gabrielbotao.softwarevcv.core.ui.components.TrustBadges
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.strings.Strings
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeColor
import com.gabrielbotao.softwarevcv.presentation.features.common.productBadgeLabel
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiState

private val CardWidth = 240.dp
private val HeroHeightCompact = 420.dp
private val HeroHeightMedium = 480.dp
private val HeroHeightExpanded = 560.dp

/**
 * Placeholder **campaign art** for the hero band — wide (landscape) **boutique / clothing-rack** shots so
 * the full-width band composes properly (not a hard-cropped portrait) and clearly reads as *a clothing
 * store*, not a random lifestyle photo (VCV-30). Curated Unsplash CDN URLs (stable per photo id, CORS-ok).
 * Replace with Felipe's real campaign photography later (swap these URLs). Hero imagery is chrome, not
 * catalog content, so it lives here rather than in the product/content layer.
 */
private val heroImages = listOf(
    "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1200&h=675&fit=crop&q=80",
    "https://images.unsplash.com/photo-1555529771-835f59fc5efe?w=1200&h=675&fit=crop&q=80",
    "https://images.unsplash.com/photo-1665815844395-06f64f44b5e3?w=1200&h=675&fit=crop&q=80",
)

private val homeTrustBadges = Strings.Home.trust.map { TrustBadge(it.first, it.second) }

/**
 * Home — the brand's first impression. Stateless: the route owns the ViewModel and passes [state] +
 * callbacks (VCV-28). See [[VCV Screens-and-UX]] §1.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onProduct: (String) -> Unit,
    onCatalog: () -> Unit,
    onAtelier: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Hero(images = heroImages, onCatalog = onCatalog)
        RevealOnAppear { TrustBadges(homeTrustBadges) }
        when {
            state.isLoading -> LoadingSection()
            state.error != null -> ErrorSection(message = state.error, onRetry = onRetry)
            else -> RevealOnAppear(delayMillis = 80) { FeaturedSection(products = state.featured, onProduct = onProduct) }
        }
        RevealOnAppear(delayMillis = 160) { AtelierTeaser(onAtelier = onAtelier) }
        Spacer(Modifier.height(Vcv.spacing.xl))
        AppFooter()
    }
}

@Composable
private fun Hero(images: List<String>, onCatalog: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val heroHeight = when (WindowWidthClass.of(maxWidth)) {
            WindowWidthClass.COMPACT -> HeroHeightCompact
            WindowWidthClass.MEDIUM -> HeroHeightMedium
            WindowWidthClass.EXPANDED -> HeroHeightExpanded
        }
        Column {
            if (images.isNotEmpty()) {
                ImageCarousel(imageUrls = images, modifier = Modifier.fillMaxWidth().height(heroHeight))
            }
            Column(
                modifier = Modifier.padding(Vcv.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
            ) {
                Text(Strings.Home.heroTitle, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                Text(Strings.Home.heroSubtitle, style = MaterialTheme.typography.bodyLarge, color = Vcv.colors.muted)
                VcvButton(text = Strings.Home.heroCta, onClick = onCatalog)
            }
        }
    }
}

@Composable
private fun FeaturedSection(products: List<Product>, onProduct: (String) -> Unit) {
    if (products.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        SectionHeader(title = Strings.Home.featured, modifier = Modifier.padding(horizontal = Vcv.spacing.lg))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Vcv.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
        ) {
            items(products) { product ->
                val badge = product.badges.firstOrNull()
                ProductCard(
                    name = product.name,
                    priceCents = product.price.amountCents,
                    imageUrl = product.cover?.url,
                    badgeText = badge?.let(::productBadgeLabel),
                    badgeColor = badge?.let { productBadgeColor(it) } ?: MaterialTheme.colorScheme.primary,
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
        SectionHeader(title = Strings.Home.atelierTitle, subtitle = Strings.Home.atelierSubtitle)
        VcvOutlinedButton(text = Strings.Home.atelierCta, onClick = onAtelier)
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
        VcvButton(text = Strings.Common.retry, onClick = onRetry)
    }
}

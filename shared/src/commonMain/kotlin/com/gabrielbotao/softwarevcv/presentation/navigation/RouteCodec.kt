package com.gabrielbotao.softwarevcv.presentation.navigation

/**
 * The single source of the route ↔ URL-path mapping. Used by the web-history bridge to keep the browser
 * URL in sync and to resolve deep links on load. `fromPath(toPath(route)) == route` for every route
 * (see `RouteCodecTest`). Unknown paths fall back to [AppRoute.Home]. See [[MVVM-Multiplatform]].
 */
object RouteCodec {

    private const val Brand = "VCV — Veste Com Você"

    /**
     * Document title for a route (`"Catálogo · VCV — Veste Com Você"`), set by the web-history bridge on
     * every navigation so browser tabs and shared links are labelled per page. Product/collection titles
     * humanise the URL slug (`vestido-zebra` → `Vestido Zebra`). See [[VCV-34]].
     */
    fun title(route: AppRoute): String = when (route) {
        AppRoute.Home -> Brand
        AppRoute.Collections -> "Coleções · $Brand"
        is AppRoute.Collection -> "${route.slug.humanize()} · $Brand"
        AppRoute.Catalog -> "Catálogo · $Brand"
        is AppRoute.Product -> "${route.id.humanize()} · $Brand"
        AppRoute.Atelier -> "Atelier · $Brand"
        AppRoute.Contact -> "Contato · $Brand"
        AppRoute.Cart -> "Sacola · $Brand"
        AppRoute.Checkout -> "Checkout · $Brand"
    }

    private fun String.humanize(): String =
        split('-').filter { it.isNotBlank() }.joinToString(" ") { part ->
            part.replaceFirstChar { it.uppercaseChar() }
        }

    fun toPath(route: AppRoute): String = when (route) {
        AppRoute.Home -> "/"
        AppRoute.Collections -> "/colecoes"
        is AppRoute.Collection -> "/colecao/${route.slug}"
        AppRoute.Catalog -> "/catalogo"
        is AppRoute.Product -> "/produto/${route.id}"
        AppRoute.Atelier -> "/atelier"
        AppRoute.Contact -> "/contato"
        AppRoute.Cart -> "/carrinho"
        AppRoute.Checkout -> "/checkout"
    }

    fun fromPath(path: String): AppRoute {
        val clean = path.substringBefore('?').substringBefore('#').trim('/')
        val segments = if (clean.isEmpty()) emptyList() else clean.split('/')
        return when {
            segments.isEmpty() -> AppRoute.Home
            segments.size == 1 -> when (segments[0]) {
                "colecoes" -> AppRoute.Collections
                "catalogo" -> AppRoute.Catalog
                "atelier" -> AppRoute.Atelier
                "contato" -> AppRoute.Contact
                "carrinho" -> AppRoute.Cart
                "checkout" -> AppRoute.Checkout
                else -> AppRoute.Home
            }
            segments.size == 2 && segments[0] == "colecao" -> AppRoute.Collection(segments[1])
            segments.size == 2 && segments[0] == "produto" -> AppRoute.Product(segments[1])
            else -> AppRoute.Home
        }
    }
}

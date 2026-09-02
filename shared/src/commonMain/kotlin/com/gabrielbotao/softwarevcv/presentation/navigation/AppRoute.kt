package com.gabrielbotao.softwarevcv.presentation.navigation

/**
 * The app's type-safe route hierarchy. Each route maps 1:1 to a URL via [RouteCodec], so pages are
 * shareable, deep-linkable, and back/forward works on web. See [[MVVM-Multiplatform]] §Navigation.
 */
sealed interface AppRoute {
    data object Home : AppRoute
    data object Collections : AppRoute
    data class Collection(val slug: String) : AppRoute
    data object Catalog : AppRoute
    data class Product(val id: String) : AppRoute
    data object Atelier : AppRoute
    data object Contact : AppRoute
}

/** Human label (pt-BR) for nav links / placeholders. */
fun AppRoute.label(): String = when (this) {
    AppRoute.Home -> "Início"
    AppRoute.Collections -> "Coleções"
    is AppRoute.Collection -> "Coleção ${slug}"
    AppRoute.Catalog -> "Catálogo"
    is AppRoute.Product -> "Produto ${id}"
    AppRoute.Atelier -> "Atelier"
    AppRoute.Contact -> "Contato"
}

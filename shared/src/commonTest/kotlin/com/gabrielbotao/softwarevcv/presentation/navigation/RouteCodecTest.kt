package com.gabrielbotao.softwarevcv.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteCodecTest {

    private val routes = listOf(
        AppRoute.Home,
        AppRoute.Collections,
        AppRoute.Collection("verao-2026"),
        AppRoute.Catalog,
        AppRoute.Product("vestido-zebra"),
        AppRoute.Atelier,
        AppRoute.Contact,
        AppRoute.Cart,
    )

    @Test
    fun path_round_trips_for_every_route() {
        routes.forEach { route ->
            val path = RouteCodec.toPath(route)
            assertEquals(route, RouteCodec.fromPath(path), "round-trip failed for $route via '$path'")
        }
    }

    @Test
    fun known_paths_map_to_routes() {
        assertEquals(AppRoute.Home, RouteCodec.fromPath("/"))
        assertEquals(AppRoute.Catalog, RouteCodec.fromPath("/catalogo"))
        assertEquals(AppRoute.Product("zebra"), RouteCodec.fromPath("/produto/zebra"))
        assertEquals(AppRoute.Collection("inverno"), RouteCodec.fromPath("/colecao/inverno"))
    }

    @Test
    fun unknown_or_dirty_paths_fall_back_to_home() {
        assertEquals(AppRoute.Home, RouteCodec.fromPath("/nao-existe"))
        assertEquals(AppRoute.Home, RouteCodec.fromPath(""))
        // query/hash are ignored
        assertEquals(AppRoute.Catalog, RouteCodec.fromPath("/catalogo?utm=ig#top"))
    }
}

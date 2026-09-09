package com.gabrielbotao.softwarevcv

import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import com.gabrielbotao.softwarevcv.presentation.navigation.RouteCodec
import kotlinx.browser.document
import kotlinx.browser.window

actual fun installWebHistory(navigator: Navigator) {
    // Base path for a sub-path deploy (GitHub Pages project site → "/Software-VCV"); "" at root.
    // Derived from the <base href> (via document.baseURI) so the same build works locally and on Pages.
    val basePath = (document.baseURI ?: "").removePrefix(window.location.origin).trimEnd('/')

    fun routeOf(pathname: String): AppRoute = RouteCodec.fromPath(pathname.removePrefix(basePath))
    fun urlOf(route: AppRoute): String = basePath + RouteCodec.toPath(route)

    // 1. Deep link on load: URL → route (before the first frame) + label the tab.
    val initial = routeOf(window.location.pathname)
    navigator.syncFromHistory(initial)
    document.title = RouteCodec.title(initial)

    // 2. App navigation → push the URL (guard against redundant pushes / popstate loops) + update title.
    navigator.onNavigated = { route ->
        val url = urlOf(route)
        if (url != window.location.pathname) {
            window.history.pushState(null, "", url)
        }
        document.title = RouteCodec.title(route)
    }

    // 3. Browser back/forward → route (without re-pushing) + update title.
    window.onpopstate = { _ ->
        val route = routeOf(window.location.pathname)
        navigator.syncFromHistory(route)
        document.title = RouteCodec.title(route)
    }
}

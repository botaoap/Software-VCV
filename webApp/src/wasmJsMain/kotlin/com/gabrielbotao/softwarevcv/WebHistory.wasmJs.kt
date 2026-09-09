package com.gabrielbotao.softwarevcv

import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import com.gabrielbotao.softwarevcv.presentation.navigation.RouteCodec
import kotlinx.browser.document
import kotlinx.browser.window

actual fun installWebHistory(navigator: Navigator) {
    // 1. Deep link on load: URL → route (before the first frame) + label the tab.
    val initial = RouteCodec.fromPath(window.location.pathname)
    navigator.syncFromHistory(initial)
    document.title = RouteCodec.title(initial)

    // 2. App navigation → push the URL (guard against redundant pushes / popstate loops) + update title.
    navigator.onNavigated = { route ->
        val path = RouteCodec.toPath(route)
        if (path != window.location.pathname) {
            window.history.pushState(null, "", path)
        }
        document.title = RouteCodec.title(route)
    }

    // 3. Browser back/forward → route (without re-pushing) + update title.
    window.onpopstate = { _ ->
        val route = RouteCodec.fromPath(window.location.pathname)
        navigator.syncFromHistory(route)
        document.title = RouteCodec.title(route)
    }
}

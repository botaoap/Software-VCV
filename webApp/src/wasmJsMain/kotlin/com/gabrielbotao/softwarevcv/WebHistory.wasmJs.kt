package com.gabrielbotao.softwarevcv

import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import com.gabrielbotao.softwarevcv.presentation.navigation.RouteCodec
import kotlinx.browser.window

actual fun installWebHistory(navigator: Navigator) {
    // 1. Deep link on load: URL → route (before the first frame).
    navigator.syncFromHistory(RouteCodec.fromPath(window.location.pathname))

    // 2. App navigation → push the URL (guard against redundant pushes / popstate loops).
    navigator.onNavigated = { route ->
        val path = RouteCodec.toPath(route)
        if (path != window.location.pathname) {
            window.history.pushState(null, "", path)
        }
    }

    // 3. Browser back/forward → route (without re-pushing).
    window.onpopstate = { _ ->
        navigator.syncFromHistory(RouteCodec.fromPath(window.location.pathname))
    }
}

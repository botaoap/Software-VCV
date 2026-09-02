package com.gabrielbotao.softwarevcv.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * The shell-owned navigator. Wraps the Navigation 3 back stack (a [SnapshotStateList] of [AppRoute]
 * keys) that `NavDisplay` renders and Compose observes. Created once — in `App` by default, or in the
 * web entrypoint so the browser-history bridge mutates the same instance. Screens receive typed nav
 * lambdas, never this object or a nav controller. See [[MVVM-Multiplatform]] §Navigation.
 */
class Navigator(initial: AppRoute = AppRoute.Home) {

    /** The live back stack; hand this to `NavDisplay(backStack = …)`. */
    val backStack: SnapshotStateList<AppRoute> = mutableStateListOf(initial)

    /** Current (top) route. */
    val current: AppRoute get() = backStack.lastOrNull() ?: AppRoute.Home

    /**
     * Fired when navigation changes the route **from within the app** (navigate/replace/pop) — the web
     * bridge sets this to push the browser URL. Not fired by [syncFromHistory] (avoids a push↔pop loop).
     */
    var onNavigated: ((AppRoute) -> Unit)? = null

    fun navigate(route: AppRoute) {
        backStack.add(route)
        onNavigated?.invoke(route)
    }

    fun replace(route: AppRoute) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
        onNavigated?.invoke(route)
    }

    /** Pops the back stack. Returns false if already at the root (caller may exit / do nothing). */
    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        val top = backStack.last()
        onNavigated?.invoke(top)
        return true
    }

    /** Update the current route from a browser back/forward event — without re-notifying the bridge. */
    fun syncFromHistory(route: AppRoute) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
    }
}

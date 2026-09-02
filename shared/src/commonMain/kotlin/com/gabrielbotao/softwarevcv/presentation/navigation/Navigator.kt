package com.gabrielbotao.softwarevcv.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The shell-owned navigator: a back stack exposed as [current] for Compose to observe. Screens receive
 * typed navigation lambdas, never this object's internals or a platform nav controller. Created once
 * (in `App`, or in the web entrypoint so the history bridge shares the instance). See
 * [[MVVM-Multiplatform]] §Navigation.
 */
class Navigator(initial: AppRoute = AppRoute.Home) {

    private val backStack = mutableListOf(initial)
    private val _current = MutableStateFlow(initial)
    val current: StateFlow<AppRoute> = _current.asStateFlow()

    /**
     * Fired when navigation changes the route **from within the app** (navigate/replace/pop) — the web
     * bridge sets this to push the browser URL. Not fired by [syncFromHistory] (avoids a push↔pop loop).
     */
    var onNavigated: ((AppRoute) -> Unit)? = null

    fun navigate(route: AppRoute) {
        backStack.add(route)
        _current.value = route
        onNavigated?.invoke(route)
    }

    fun replace(route: AppRoute) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
        _current.value = route
        onNavigated?.invoke(route)
    }

    /** Pops the back stack. Returns false if already at the root (caller may exit / do nothing). */
    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        val top = backStack.last()
        _current.value = top
        onNavigated?.invoke(top)
        return true
    }

    /** Update the current route from a browser back/forward event — without re-notifying the bridge. */
    fun syncFromHistory(route: AppRoute) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
        _current.value = route
    }
}

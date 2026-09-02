package com.gabrielbotao.softwarevcv

import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/**
 * Binds the browser URL to the [Navigator]: resolves the initial deep link on load, pushes the URL when
 * the app navigates, and follows browser back/forward (popstate). Implemented per web target (js/wasm)
 * because `kotlinx.browser` differs between them. See [[MVVM-Multiplatform]] §Navigation.
 */
expect fun installWebHistory(navigator: Navigator)

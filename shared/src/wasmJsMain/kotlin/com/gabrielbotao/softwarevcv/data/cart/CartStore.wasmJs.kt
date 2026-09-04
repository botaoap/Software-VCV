package com.gabrielbotao.softwarevcv.data.cart

import kotlinx.browser.localStorage

private const val KEY = "vcv.cart.v1"

/** Persists the cart across reloads via the browser's localStorage. */
actual fun platformCartStore(): CartStore = object : CartStore {
    override fun load(): String? = localStorage.getItem(KEY)
    override fun save(value: String) = localStorage.setItem(KEY, value)
    override fun clear() = localStorage.removeItem(KEY)
}

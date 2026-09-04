package com.gabrielbotao.softwarevcv.data.cart

/**
 * Minimal key-value persistence seam for the cart snapshot. Web persists via `localStorage`; the other
 * platforms use an in-memory store for now (native persistence — SharedPreferences / NSUserDefaults /
 * Preferences — is a follow-up). See [[VCV-29]].
 */
interface CartStore {
    fun load(): String?
    fun save(value: String)
    fun clear()
}

/** Platform-provided [CartStore] (localStorage on web, in-memory elsewhere). */
expect fun platformCartStore(): CartStore

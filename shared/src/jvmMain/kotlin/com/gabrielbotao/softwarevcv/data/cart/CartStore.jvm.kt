package com.gabrielbotao.softwarevcv.data.cart

/** In-memory store (native persistence is a follow-up). */
actual fun platformCartStore(): CartStore = object : CartStore {
    private var stored: String? = null
    override fun load(): String? = stored
    override fun save(value: String) { stored = value }
    override fun clear() { stored = null }
}

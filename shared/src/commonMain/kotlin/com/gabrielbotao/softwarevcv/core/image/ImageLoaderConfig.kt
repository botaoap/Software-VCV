package com.gabrielbotao.softwarevcv.core.image

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

/**
 * Configures Coil's singleton [ImageLoader] with a **Ktor network fetcher** so `RemoteImage`/`AsyncImage`
 * can load remote URLs on every target (the Ktor engine is provided per platform in the build). Call once
 * at app start; `setSafe` is a no-op if already set. See [[VCV Design-System]] §10.
 */
fun configureImageLoader() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
}

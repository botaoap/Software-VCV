package com.gabrielbotao.softwarevcv.data.di

import com.gabrielbotao.softwarevcv.data.cart.LocalCartRepository
import com.gabrielbotao.softwarevcv.data.cart.platformCartStore
import com.gabrielbotao.softwarevcv.data.commerce.WhatsAppCheckoutGateway
import com.gabrielbotao.softwarevcv.data.content.BundledContentSource
import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.data.repository.BrandRepositoryImpl
import com.gabrielbotao.softwarevcv.data.repository.CatalogRepositoryImpl
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutGateway
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository
import com.gabrielbotao.softwarevcv.domain.repository.CartRepository
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/** Data-layer bindings: JSON, the content source (bundled JSON), and the repositories. */
val dataModule = module {
    single { Json { ignoreUnknownKeys = true; isLenient = true } }
    single<VcvContentApi> { BundledContentSource(get()) }
    single<CatalogRepository> { CatalogRepositoryImpl(get()) }
    single<BrandRepository> { BrandRepositoryImpl(get()) }
    // Cart is shared state → single. CheckoutGateway is the commerce-agnostic seam: the MVP binds the
    // WhatsApp (links-out) prototype; swap to an own-backend / 3rd-party impl here, UI untouched (VCV-20).
    single<CartRepository> { LocalCartRepository(get(), platformCartStore()) }
    single<CheckoutGateway> { WhatsAppCheckoutGateway(get()) }
}

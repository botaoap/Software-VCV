package com.gabrielbotao.softwarevcv.domain.di

import com.gabrielbotao.softwarevcv.domain.usecase.GetAtelierUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionsUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetFeaturedProductsUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductsUseCase
import org.koin.dsl.module

/** Domain-layer bindings: one use case per operation (`factory` — a new instance per call). */
val domainModule = module {
    factory { GetCollectionsUseCase(get()) }
    factory { GetCollectionUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetProductUseCase(get()) }
    factory { GetFeaturedProductsUseCase(get()) }
    factory { GetAtelierUseCase(get()) }
    factory { GetContactUseCase(get()) }
}

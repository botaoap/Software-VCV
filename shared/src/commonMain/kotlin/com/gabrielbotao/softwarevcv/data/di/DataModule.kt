package com.gabrielbotao.softwarevcv.data.di

import com.gabrielbotao.softwarevcv.data.content.BundledContentSource
import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.data.repository.BrandRepositoryImpl
import com.gabrielbotao.softwarevcv.data.repository.CatalogRepositoryImpl
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/** Data-layer bindings: JSON, the content source (bundled JSON), and the repositories. */
val dataModule = module {
    single { Json { ignoreUnknownKeys = true; isLenient = true } }
    single<VcvContentApi> { BundledContentSource(get()) }
    single<CatalogRepository> { CatalogRepositoryImpl(get()) }
    single<BrandRepository> { BrandRepositoryImpl(get()) }
}

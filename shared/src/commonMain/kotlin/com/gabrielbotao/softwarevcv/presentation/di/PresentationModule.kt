package com.gabrielbotao.softwarevcv.presentation.di

import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel.CatalogViewModel
import com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel.CollectionsViewModel
import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
import com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel.ProductViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation-layer bindings: ViewModels. One per feature (added as pages land, VCV-6…). */
val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::CatalogViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::AtelierViewModel)
}

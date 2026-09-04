package com.gabrielbotao.softwarevcv.presentation.di

import com.gabrielbotao.softwarevcv.presentation.chrome.ChromeViewModel
import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.features.cart.viewmodel.CartViewModel
import com.gabrielbotao.softwarevcv.presentation.features.checkout.viewmodel.CheckoutViewModel
import com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel.CatalogViewModel
import com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel.CollectionsViewModel
import com.gabrielbotao.softwarevcv.presentation.features.contact.viewmodel.ContactViewModel
import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
import com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel.ProductViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation-layer bindings: ViewModels. One per feature. */
val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::CatalogViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::AtelierViewModel)
    viewModelOf(::ContactViewModel)
    viewModelOf(::ChromeViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::CheckoutViewModel)
}

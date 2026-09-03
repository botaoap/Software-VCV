package com.gabrielbotao.softwarevcv.presentation.di

import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation-layer bindings: ViewModels. One per feature (added as pages land, VCV-6…). */
val presentationModule = module {
    viewModelOf(::HomeViewModel)
}

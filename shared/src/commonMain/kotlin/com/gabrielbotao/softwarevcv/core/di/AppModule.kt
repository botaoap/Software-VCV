package com.gabrielbotao.softwarevcv.core.di

import com.gabrielbotao.softwarevcv.core.logger.AppLogger
import com.gabrielbotao.softwarevcv.core.logger.PrintAppLogger
import com.gabrielbotao.softwarevcv.core.platform.Platform
import com.gabrielbotao.softwarevcv.core.platform.getPlatform
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

/**
 * Cross-cutting singletons (logger, platform). Layer modules ([dataModule], [domainModule],
 * [presentationModule]) are composed here and filled in by later cards. See [[KMP-Clean-Architecture]] §5.
 */
val coreModule = module {
    single<AppLogger> { PrintAppLogger() }
    single<Platform> { getPlatform() }
}

/** Data layer bindings (repositories, content source, Ktor). Populated in VCV-5. */
val dataModule = module { }

/** Domain layer bindings (use cases — `factory`). Populated in VCV-5. */
val domainModule = module { }

/** Presentation layer bindings (ViewModels — `viewModel`). Populated per feature card. */
val presentationModule = module { }

/**
 * Boots the Koin graph once and returns it. Idempotent — safe to call from any platform entrypoint
 * (and repeatedly, e.g. iOS `MainViewController`): a second call returns the already-started [Koin].
 * Pass [appDeclaration] for platform extras (e.g. Android context). Uses [KoinPlatformTools] rather
 * than `GlobalContext` so it resolves on every target, Kotlin/Native included.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): Koin {
    KoinPlatformTools.defaultContext().getOrNull()?.let { return it }
    return startKoin {
        appDeclaration()
        modules(coreModule, dataModule, domainModule, presentationModule)
    }.koin
}

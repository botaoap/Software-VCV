package com.gabrielbotao.softwarevcv.core.di

import com.gabrielbotao.softwarevcv.core.logger.AppLogger
import com.gabrielbotao.softwarevcv.core.logger.PrintAppLogger
import com.gabrielbotao.softwarevcv.core.platform.Platform
import com.gabrielbotao.softwarevcv.core.platform.getPlatform
import com.gabrielbotao.softwarevcv.data.di.dataModule
import com.gabrielbotao.softwarevcv.domain.di.domainModule
import com.gabrielbotao.softwarevcv.presentation.di.presentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

/** Cross-cutting singletons (logger, platform). Layer modules live in their own layers' `di/`. */
val coreModule = module {
    single<AppLogger> { PrintAppLogger() }
    single<Platform> { getPlatform() }
}

/**
 * Boots the Koin graph once. Idempotent — safe to call from any platform entrypoint (and repeatedly,
 * e.g. iOS `MainViewController`): a second call is a no-op. Pass [appDeclaration] for platform extras.
 * Returns `Unit` so callers don't need `koin-core` on their classpath; resolve via
 * `KoinPlatformTools.defaultContext().get()` / `koinInject()`.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        appDeclaration()
        modules(coreModule, dataModule, domainModule, presentationModule)
    }
}

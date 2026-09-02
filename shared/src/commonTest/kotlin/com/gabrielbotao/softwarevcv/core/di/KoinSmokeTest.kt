package com.gabrielbotao.softwarevcv.core.di

import com.gabrielbotao.softwarevcv.core.logger.AppLogger
import com.gabrielbotao.softwarevcv.core.platform.Platform
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** VCV-1 foundation smoke test: the Koin graph boots and the core singletons resolve on every target. */
class KoinSmokeTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun koin_graph_resolves_core_singletons() {
        initKoin()
        val koin = KoinPlatformTools.defaultContext().get()

        val logger = koin.get<AppLogger>()
        val platform = koin.get<Platform>()

        assertNotNull(logger)
        assertNotNull(platform)
        assertTrue(platform.name.isNotBlank())
    }

    @Test
    fun initKoin_is_idempotent() {
        initKoin()
        initKoin() // second call must be a no-op, not a "Koin already started" crash
        assertNotNull(KoinPlatformTools.defaultContext().get().get<AppLogger>())
    }
}

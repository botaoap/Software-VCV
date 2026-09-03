package com.gabrielbotao.softwarevcv.presentation.features.atelier

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.AtelierSection
import com.gabrielbotao.softwarevcv.domain.usecase.GetAtelierUseCase
import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.features.brand.FakeBrandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AtelierViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_atelier_content_on_init() = runTest {
        val content = AtelierContent("Headline", listOf(AtelierSection("Origem", "corpo")))
        val vm = AtelierViewModel(GetAtelierUseCase(FakeBrandRepository(atelierContent = content)))
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Headline", state.content?.headline)
            assertEquals(1, state.content?.sections?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

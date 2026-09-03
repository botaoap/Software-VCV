package com.gabrielbotao.softwarevcv.presentation.features.collections

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.catalog.FakeCatalogRepository
import com.gabrielbotao.softwarevcv.presentation.features.catalog.testCollection
import com.gabrielbotao.softwarevcv.presentation.features.collections.viewmodel.CollectionsViewModel
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

class CollectionsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_collections_on_init() = runTest {
        val repo = FakeCatalogRepository(collectionList = listOf(testCollection("verao"), testCollection("essenciais")))
        val vm = CollectionsViewModel(GetCollectionsUseCase(repo))
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.collections.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

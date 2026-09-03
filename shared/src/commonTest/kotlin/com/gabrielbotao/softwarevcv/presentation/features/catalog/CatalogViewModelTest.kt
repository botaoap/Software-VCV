package com.gabrielbotao.softwarevcv.presentation.features.catalog

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.domain.usecase.GetCollectionUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.catalog.viewmodel.CatalogViewModel
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
import kotlin.test.assertNull

class CatalogViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeCatalogRepository) =
        CatalogViewModel(GetProductsUseCase(repo), GetCollectionUseCase(repo))

    @Test
    fun load_null_slug_shows_all_products_no_header() = runTest {
        val vm = viewModel(FakeCatalogRepository(listOf(testProduct("a", "c1"), testProduct("b", "c2"))))
        vm.load(null)
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.products.size)
            assertNull(state.collection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun load_slug_filters_products_and_sets_collection_header() = runTest {
        val repo = FakeCatalogRepository(
            productList = listOf(testProduct("a", "c1"), testProduct("b", "c2")),
            collectionList = listOf(testCollection("c1")),
        )
        val vm = viewModel(repo)
        vm.load("c1")
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(listOf("a"), state.products.map { it.id })
            assertEquals("c1", state.collection?.slug)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

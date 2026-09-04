package com.gabrielbotao.softwarevcv.presentation.features.product

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.data.cart.InMemoryCartRepository
import com.gabrielbotao.softwarevcv.domain.usecase.AddToCartUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.GetProductUseCase
import com.gabrielbotao.softwarevcv.presentation.features.catalog.FakeCatalogRepository
import com.gabrielbotao.softwarevcv.presentation.features.catalog.testProduct
import com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel.ProductViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_product_by_id() = runTest {
        val vm = ProductViewModel(GetProductUseCase(FakeCatalogRepository(listOf(testProduct("zebra")))), AddToCartUseCase(InMemoryCartRepository()))
        vm.load("zebra")
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("zebra", state.product?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun missing_id_is_not_found() = runTest {
        val vm = ProductViewModel(GetProductUseCase(FakeCatalogRepository(listOf(testProduct("zebra")))), AddToCartUseCase(InMemoryCartRepository()))
        vm.load("nope")
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.product)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

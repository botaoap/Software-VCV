package com.gabrielbotao.softwarevcv.presentation.features.home

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository
import com.gabrielbotao.softwarevcv.domain.usecase.GetFeaturedProductsUseCase
import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
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

private fun product(id: String, badges: List<ProductBadge>) = Product(
    id = id, name = id, collectionSlug = null, price = Money(1000), images = emptyList(),
    shortDescription = "", fabric = FabricSpec("m", "o", emptyList()), variants = emptyList(),
    badges = badges, buyUrl = null,
)

private class FakeCatalog(private val featuredList: List<Product>) : CatalogRepository {
    override suspend fun collections() = Result.success(emptyList<Collection>())
    override suspend fun collection(slug: String) = Result.failure<Collection>(NoSuchElementException())
    override suspend fun products(collectionSlug: String?) = Result.success(emptyList<Product>())
    override suspend fun product(id: String) = Result.failure<Product>(NoSuchElementException())
    override suspend fun featured() = Result.success(featuredList)
}

class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_featured_and_picks_the_best_seller() = runTest {
        val vm = HomeViewModel(
            GetFeaturedProductsUseCase(
                FakeCatalog(listOf(product("zebra", listOf(ProductBadge.BEST_SELLER)), product("x", emptyList()))),
            ),
        )
        vm.uiState.test {
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.featured.size)
            assertEquals("zebra", loaded.bestSeller?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

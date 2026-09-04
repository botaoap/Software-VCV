package com.gabrielbotao.softwarevcv.presentation.features.checkout

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.data.cart.InMemoryCartRepository
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutGateway
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutOutcome
import com.gabrielbotao.softwarevcv.domain.commerce.CustomerInfo
import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import com.gabrielbotao.softwarevcv.domain.repository.CartRepository
import com.gabrielbotao.softwarevcv.domain.usecase.CheckoutUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.ObserveCartUseCase
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.checkout.viewmodel.CheckoutViewModel
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
import kotlin.test.assertTrue

class CheckoutViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class FakeGateway(val outcome: CheckoutOutcome) : CheckoutGateway {
        override suspend fun checkout(cart: Cart, customer: CustomerInfo) = outcome
    }

    private fun product() = Product(
        id = "p", name = "Peça", collectionSlug = null, price = Money(18900),
        images = emptyList(), shortDescription = "", fabric = FabricSpec("v", "Gaspar", emptyList(), null),
        variants = emptyList(), badges = emptyList(), buyUrl = null,
    )

    private fun vm(repo: CartRepository, outcome: CheckoutOutcome) =
        CheckoutViewModel(ObserveCartUseCase(repo), CheckoutUseCase(repo, FakeGateway(outcome)))

    @Test
    fun cannot_submit_without_name_or_with_empty_cart() = runTest {
        val repo = InMemoryCartRepository()
        val model = vm(repo, CheckoutOutcome.Confirmed("x"))
        assertFalse(model.uiState.value.canSubmit) // empty cart + blank name
        repo.add(product(), Size(40))
        model.onEvent(CheckoutUiEvent.NameChanged("Maria"))
        assertTrue(model.uiState.value.canSubmit)
    }

    @Test
    fun handoff_outcome_marks_done_and_sets_openUrl() = runTest {
        val repo = InMemoryCartRepository().apply { add(product(), Size(40)) }
        val model = vm(repo, CheckoutOutcome.ExternalHandoff("https://wa.me/55?text=abc"))
        model.onEvent(CheckoutUiEvent.NameChanged("Maria"))
        model.onEvent(CheckoutUiEvent.Submit)
        model.uiState.test {
            val s = awaitItem()
            assertTrue(s.done)
            assertEquals("https://wa.me/55?text=abc", s.openUrl)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmed_outcome_sets_reference_and_clears_cart() = runTest {
        val repo = InMemoryCartRepository().apply { add(product(), Size(40)) }
        val model = vm(repo, CheckoutOutcome.Confirmed("VCV-1001"))
        model.onEvent(CheckoutUiEvent.NameChanged("Maria"))
        model.onEvent(CheckoutUiEvent.Submit)
        assertEquals("VCV-1001", model.uiState.value.confirmationRef)
        assertTrue(model.uiState.value.done)
        assertTrue(repo.cart.value.isEmpty) // CheckoutUseCase clears on Confirmed
    }
}

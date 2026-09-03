package com.gabrielbotao.softwarevcv.presentation.features.contact

import app.cash.turbine.test
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import com.gabrielbotao.softwarevcv.presentation.features.brand.FakeBrandRepository
import com.gabrielbotao.softwarevcv.presentation.features.contact.viewmodel.ContactViewModel
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

class ContactViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loads_contact_content_on_init() = runTest {
        val contact = ContactContent(whatsapp = "https://wa.me/55", instagram = null, email = "a@b.com", whereToBuy = listOf("Gaspar"))
        val vm = ContactViewModel(GetContactUseCase(FakeBrandRepository(contactContent = contact)))
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("https://wa.me/55", state.content?.whatsapp)
            assertEquals(listOf("Gaspar"), state.content?.whereToBuy)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

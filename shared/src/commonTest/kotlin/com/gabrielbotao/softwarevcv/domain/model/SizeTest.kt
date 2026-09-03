package com.gabrielbotao.softwarevcv.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SizeTest {

    @Test
    fun valid_sizes_are_accepted() {
        assertEquals(38, Size(38).value)
        assertEquals(52, Size(52).value)
    }

    @Test
    fun out_of_range_size_is_rejected() {
        assertFailsWith<IllegalArgumentException> { Size(30) }
        assertFailsWith<IllegalArgumentException> { Size(70) }
    }
}

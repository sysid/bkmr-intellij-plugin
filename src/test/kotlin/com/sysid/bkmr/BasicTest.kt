package com.sysid.bkmr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Minimal test with no external dependencies to verify test infrastructure
 */
class BasicTest {

    @Test
    fun `basic test should pass`() {
        assertEquals(2, 1 + 1)
    }

    @Test  
    fun `string test should pass`() {
        assertEquals("hello", "hello")
    }

    @Test
    fun `list test should pass`() {
        val list = listOf(1, 2, 3)
        assertEquals(3, list.size)
        assertEquals(2, list[1])
    }
}
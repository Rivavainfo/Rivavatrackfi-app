package com.rivavafi.universal.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {

    @Test
    fun testValidE164PhoneNumbers() {
        val e164Regex = Regex("^\\+[1-9]\\d{1,14}\$")
        assertTrue(e164Regex.matches("+919876543210"))
        assertTrue(e164Regex.matches("+14155552671"))
        assertTrue(e164Regex.matches("+447911123456"))
    }

    @Test
    fun testInvalidE164PhoneNumbers() {
        val e164Regex = Regex("^\\+[1-9]\\d{1,14}\$")
        assertFalse(e164Regex.matches("919876543210")) // Missing +
        assertFalse(e164Regex.matches("+019876543210")) // Starts with +0
        assertFalse(e164Regex.matches("+91 9876543210")) // Contains space
        assertFalse(e164Regex.matches("+919876543210123456")) // Too long
        assertFalse(e164Regex.matches("+")) // Too short
    }
}

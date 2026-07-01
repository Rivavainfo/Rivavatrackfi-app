package com.rivavafi.universal.domain.preferences

import org.junit.Test
import org.junit.Assert.assertEquals

class SmsTrackingModeTest {
    @Test
    fun testEnumValues() {
        assertEquals("CREDIT_ONLY", SmsTrackingMode.CREDIT_ONLY.name)
        assertEquals("DEBIT_ONLY", SmsTrackingMode.DEBIT_ONLY.name)
        assertEquals("BOTH", SmsTrackingMode.BOTH.name)
    }
}

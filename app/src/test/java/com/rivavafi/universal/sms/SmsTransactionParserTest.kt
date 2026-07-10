package com.rivavafi.universal.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsTransactionParserTest {

    @Test
    fun testHdfcDebitMessage() {
        val body = "UPDATE: INR 500.00 has been debited from your HDFC Bank A/c... available balance is INR 1,234.50."
        val result = SmsTransactionParser.parseMessage("HDFC", body, 12345L, "1", SmsTrackingMode.BOTH)

        assertNotNull(result)
        assertEquals(500.0, result?.amount ?: 0.0, 0.001)
        assertEquals("DEBIT", result?.type)
        assertEquals(1234.50, result?.availableBalance ?: 0.0, 0.001)
        assertEquals("HDFC Bank", result?.bankName)
    }

    @Test
    fun testSalaryCreditMessage() {
        val body = "Dear Customer, INR 55,000.00 credited to your A/c ... via NEFT ... available balance is INR 60,000."
        val result = SmsTransactionParser.parseMessage("SBI", body, 12345L, "2", SmsTrackingMode.BOTH)

        assertNotNull(result)
        assertEquals(55000.0, result?.amount ?: 0.0, 0.001)
        assertEquals("CREDIT", result?.type)
        assertEquals("SBI", result?.bankName)
    }

    @Test
    fun testOtpMessageIgnored() {
        val body = "Your OTP for login is 123456. Do not share it with anyone."
        val result = SmsTransactionParser.parseMessage("HDFC", body, 12345L, "3", SmsTrackingMode.BOTH)
        assertNull(result)
    }

    @Test
    fun testModeFiltering() {
        val creditBody = "Rs. 100 credited to your account"
        val debitBody = "Rs. 50 spent on card"

        val creditOnlyMode = SmsTransactionParser.parseMessage("AXIS", creditBody, 0L, "1", SmsTrackingMode.CREDIT_ONLY)
        assertNotNull(creditOnlyMode)

        val debitIgnored = SmsTransactionParser.parseMessage("AXIS", debitBody, 0L, "2", SmsTrackingMode.CREDIT_ONLY)
        assertNull(debitIgnored)

        val creditIgnored = SmsTransactionParser.parseMessage("AXIS", creditBody, 0L, "3", SmsTrackingMode.DEBIT_ONLY)
        assertNull(creditIgnored)

        val debitOnlyMode = SmsTransactionParser.parseMessage("AXIS", debitBody, 0L, "4", SmsTrackingMode.DEBIT_ONLY)
        assertNotNull(debitOnlyMode)
    }
}

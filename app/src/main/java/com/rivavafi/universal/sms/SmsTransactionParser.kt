package com.rivavafi.universal.sms

import java.util.regex.Pattern

object SmsTransactionParser {

    private val CREDIT_WORDS = listOf("credited", "credit", "deposited", "received", "refund", "cashback", "salary", "interest")
    private val DEBIT_WORDS = listOf("debited", "debit", "spent", "paid", "withdrawn", "purchase", "sent", "upi", "imps", "atm")

    // Amount extraction: e.g. Rs. 500, INR 500.00
    private val amountRegex = Pattern.compile("(?i)(?:INR|Rs\\.?|₹)\\s*([0-9,]+(?:\\.\\d{1,2})?)")

    // Balance extraction
    private val balanceRegex = Pattern.compile("(?i)(?:available balance|avl bal|bal|balance).*?(?:INR|Rs\\.?|₹)\\s*([0-9,]+(?:\\.\\d{1,2})?)")

    // Reference extraction
    private val refRegex = Pattern.compile("(?i)(?:ref|reference|utr|txn|transaction id)[:\\s.-]*([A-Z0-9]+)")

    fun parseMessage(
        sender: String,
        body: String,
        timestamp: Long,
        platformSmsId: String?,
        mode: SmsTrackingMode
    ): ParsedSmsTransaction? {
        if (mode == SmsTrackingMode.OFF) return null

        val lowerBody = body.lowercase()

        // Skip OTPs and promotional messages
        if (lowerBody.contains("otp") || lowerBody.contains("verification code")) return null

        val isCredit = CREDIT_WORDS.any { lowerBody.contains(it) }
        val isDebit = DEBIT_WORDS.any { lowerBody.contains(it) }

        // Discard ambiguous messages if strictly ambiguous
        if (isCredit && isDebit) {
            if (lowerBody.contains("credited") && !lowerBody.contains("debited")) {
                // assume credit
            } else if (lowerBody.contains("debited") && !lowerBody.contains("credited")) {
                // assume debit
            } else {
                return null
            }
        }
        if (!isCredit && !isDebit) return null

        val type = if (lowerBody.contains("credited")) "CREDIT" else if (lowerBody.contains("debited")) "DEBIT" else if (isCredit) "CREDIT" else "DEBIT"

        // Apply tracking mode filter
        if (mode == SmsTrackingMode.CREDIT_ONLY && type == "DEBIT") return null
        if (mode == SmsTrackingMode.DEBIT_ONLY && type == "CREDIT") return null

        // Extract amount
        var balance: Double? = null
        val balMatcher = balanceRegex.matcher(body)
        if (balMatcher.find()) {
            balance = balMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        var amountStr: String? = null
        val matcher = amountRegex.matcher(body)
        val amounts = mutableListOf<Double>()
        while (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull()?.let { amounts.add(it) }
        }

        val candidates = amounts.filter { it != balance }
        if (candidates.isEmpty()) {
            if (amounts.isNotEmpty() && amounts.first() > 0) {
                amountStr = amounts.first().toString()
            } else {
                return null
            }
        } else {
            amountStr = candidates.first().toString()
        }

        if (amountStr == null) return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        // Extract reference
        var refId: String? = null
        val refMatcher = refRegex.matcher(body)
        if (refMatcher.find()) {
            refId = refMatcher.group(1)
        }

        val bankName = extractBankName(sender)

        return ParsedSmsTransaction(
            amount = amount,
            type = type,
            merchantName = extractMerchantName(body) ?: "Unknown Merchant",
            bankName = bankName,
            category = if (type == "CREDIT") "Income" else "Expense", // Simplified category
            date = timestamp,
            smsId = platformSmsId,
            rawMessage = body,
            availableBalance = balance,
            referenceId = refId
        )
    }

    private fun extractBankName(sender: String): String? {
        val s = sender.uppercase()
        if (s.contains("HDFC")) return "HDFC Bank"
        if (s.contains("SBI") || s.contains("SBIPSG")) return "SBI"
        if (s.contains("ICICI")) return "ICICI Bank"
        if (s.contains("AXIS")) return "Axis Bank"
        if (s.contains("KOTAK")) return "Kotak Bank"
        if (s.contains("PNB")) return "PNB"
        return sender.takeIf { it.isNotBlank() }
    }

    private fun extractMerchantName(body: String): String? {
        // Very basic extraction logic
        val toMatch = Pattern.compile("(?i)(?:vpa|to|info|at)\\s+([A-Z0-9@.-]+)")
        val matcher = toMatch.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return "Unknown"
    }
}

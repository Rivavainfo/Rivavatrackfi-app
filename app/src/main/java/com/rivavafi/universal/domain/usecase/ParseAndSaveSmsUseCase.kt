package com.rivavafi.universal.domain.usecase

import android.util.Log
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.data.local.UserCorrectionDao
import com.rivavafi.universal.domain.repository.TransactionRepository
import java.util.regex.Pattern
import javax.inject.Inject

class ParseAndSaveSmsUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val offlineClassifier: OfflineClassifier,
    private val userCorrectionDao: UserCorrectionDao
) {
    // Enhanced regex using requested format to handle formats like "Rs...", "INR...", etc.
    // Also handling case where amount comes first, like "1,000.00 deposited"
    private val amountPattern = Pattern.compile("(?i)(?:(?:₹|Rs\\.?|INR)\\s?([0-9,]+(?:\\.[0-9]{1,2})?))|(?:([0-9,]+(?:\\.[0-9]{1,2})?)\\s?(?:INR|Rs\\.?|₹))")

    // Ignore Keywords
    private val ignoreKeywords = listOf("otp", "verification code", "marketing", "card inactivity", "activation reminder", "reminder")

    // Transaction Keywords
    private val expenseKeywords = listOf("debited", "spent", "sent", "paid", "purchase", "withdrawn")
    private val incomeKeywords = listOf("credited", "received", "deposited", "added")
    private val upiKeywords = listOf("upi", "upi ref", "upi ref no", "upi txn")

    // Extracting merchant/entity (e.g., "paid to Amazon", "sent to Ramesh", "UPI to Swiggy")
    private val merchantPattern = Pattern.compile("(?i)(?:paid to|sent to|upi to|at|to|via)\\s+([A-Za-z0-9\\s&\\*\\.@_-]+?)(?:on|ref|\\.|$)")

    // Bank Name Detectors (Including Sender Code Extractions like VM-KOTAKB)
    private val bankPatterns = mapOf(
        "HDFC" to Pattern.compile("(?i)(?:hdfc|hdfcbk)"),
        "SBI" to Pattern.compile("(?i)(?:sbi|state bank|sbin|sbiupi|vk-sbiinb)"),
        "ICICI" to Pattern.compile("(?i)(?:icici|icicib)"),
        "Axis" to Pattern.compile("(?i)(?:axis|axisbk)"),
        "Kotak" to Pattern.compile("(?i)(?:kotak|kotakb)"),
        "YES Bank" to Pattern.compile("(?i)(?:yesbank|yesbnk)"),
        "IDFC" to Pattern.compile("(?i)(?:idfc|idfcfb)"),
        "Canara" to Pattern.compile("(?i)(?:canara|canbnk)"),
        "PNB" to Pattern.compile("(?i)(?:pnb|punjab national)"),
        "BOB" to Pattern.compile("(?i)(?:bob|bank of baroda)"),
        "Paytm" to Pattern.compile("(?i)(?:paytm)"),
        "GPay" to Pattern.compile("(?i)(?:gpay)"),
        "PhonePe" to Pattern.compile("(?i)(?:phonepe)"),
        "UPI" to Pattern.compile("(?i)(?:upi)")
    )

    // Balance Detectors (e.g., "Available balance is Rs 500.00", "Bal: INR 100")
    private val balancePattern = Pattern.compile("(?i)(?:bal|balance|avl bal|available balance).*?(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)")

    suspend operator fun invoke(sender: String, messageBody: String, timestamp: Long, smsId: String? = null) {
        val transaction = parseAndReturnSuspend(sender, messageBody, timestamp, smsId)
        if (transaction == null) {
            Log.d("TRACKFI_PARSER", "Ignored SMS: Could not detect transaction info. Sender: $sender")
            return
        }

        try {
            // For Subscriptions: Check historical data
            var updatedTransaction = transaction
            if (transaction.category == "SUBSCRIPTION") {
                val pastTransactions = repository.getTransactionsByMerchant(transaction.merchantName)
                if (pastTransactions.isNotEmpty()) {
                    // Similar amount logic
                    val similarTxn = pastTransactions.find { Math.abs(it.amount - transaction.amount) < (transaction.amount * 0.1) }
                    if (similarTxn != null) {
                        val timeDiff = transaction.date - similarTxn.date
                        val daysDiff = timeDiff / (1000 * 60 * 60 * 24)
                        val billingCycle = if (daysDiff in 25..35) "MONTHLY" else if (daysDiff in 360..370) "YEARLY" else "UNKNOWN"

                        updatedTransaction = transaction.copy(
                            billingCycle = billingCycle,
                            lastPaymentDate = transaction.date
                        )
                    }
                }
            }

            // Deduplicate explicitly before insert
            if (!repository.doesTransactionExist(updatedTransaction.date, updatedTransaction.amount, updatedTransaction.merchantName)) {
                repository.addTransaction(updatedTransaction)
                Log.d("TRACKFI_DATABASE", "Successfully saved transaction: $updatedTransaction")
            } else {
                Log.d("TRACKFI_DATABASE", "Duplicate transaction detected in parser: $updatedTransaction")
            }
        } catch (e: Exception) {
            Log.e("TRACKFI_DATABASE", "Error saving transaction: ${e.message}")
        }
    }

    // Legacy non-suspend method (if used elsewhere, defaults to skipping layer 3)
    fun parseAndReturn(sender: String, messageBody: String, timestamp: Long, smsId: String? = null): TransactionEntity? {
        // Warning: This won't hit the DB for user corrections. Use parseAndReturnSuspend in real flows.
        return parseInternal(sender, messageBody, timestamp, smsId, null)
    }

    suspend fun parseAndReturnSuspend(sender: String, messageBody: String, timestamp: Long, smsId: String? = null): TransactionEntity? {
        val merchant = extractMerchant(messageBody, sender)
        val corrections = userCorrectionDao.getCorrectionsForMerchant(merchant)

        // Find matching correction
        var matchedCorrection = corrections.firstOrNull { it.keyword == null } // Default merchant match
        for (correction in corrections) {
            if (correction.keyword != null && messageBody.lowercase().contains(correction.keyword.lowercase())) {
                matchedCorrection = correction
                break
            }
        }

        return parseInternal(sender, messageBody, timestamp, smsId, matchedCorrection)
    }

    private fun parseInternal(sender: String, messageBody: String, timestamp: Long, smsId: String?, matchedCorrection: com.rivavafi.universal.data.local.UserCorrectionEntity?): TransactionEntity? {
        val lowerBody = messageBody.lowercase()
        if (ignoreKeywords.any { lowerBody.contains(it) }) {
            return null
        }

        // Layer 1: Rule Engine
        val isMandateCancelled = listOf("mandate cancelled", "mandate revoked", "mandate stopped").any { lowerBody.contains(it) }
        val isMandateCreated = listOf("mandate created", "autopay mandate", "emandate registered").any { lowerBody.contains(it) }

        val amountAndType = extractAmountAndType(messageBody)
        val merchant = extractMerchant(messageBody, sender)

        if (amountAndType == null) {
            if (isMandateCancelled) {
                return TransactionEntity(
                    merchantName = merchant,
                    amount = 0.0,
                    type = "MANDATE_CANCELLED",
                    category = "MANDATE_CANCELLED",
                    date = timestamp,
                    smsId = smsId,
                    bankName = extractBankName(sender, messageBody),
                    availableBalance = extractBalance(messageBody),
                    rawMessage = messageBody,
                    referenceId = extractReferenceId(messageBody)
                )
            } else if (isMandateCreated) {
                return TransactionEntity(
                    merchantName = merchant,
                    amount = 0.0, // Or extract if available
                    type = "MANDATE_CREATED",
                    category = "MANDATE_CREATED",
                    date = timestamp,
                    smsId = smsId,
                    bankName = extractBankName(sender, messageBody),
                    availableBalance = extractBalance(messageBody),
                    rawMessage = messageBody,
                    referenceId = extractReferenceId(messageBody)
                )
            }
            return null
        }

        var amount = amountAndType.first
        var type = amountAndType.second
        val bankName = extractBankName(sender, messageBody)
        val balance = extractBalance(messageBody)
        val referenceId = extractReferenceId(messageBody)

        var category = autoCategorize(merchant, messageBody)
        var subcategory: String?

        // Layer 3: User Corrections (Highest Priority over AI/Rules)
        if (matchedCorrection != null) {
            category = matchedCorrection.category
            subcategory = matchedCorrection.subcategory
            // Adjust type based on user forced category if needed
            if (category == "EXPENSE" || category == "INCOME") {
                type = category
            } else if (category == "BILL_PENDING" || category == "SELF_TRANSFER" || category == "MANDATE_CREATED") {
                type = category
            }
        } else {
            // Layer 2 & 1: AI / Rule Engine Fallback
            val (aiCategory, aiConfidence) = offlineClassifier.classify(messageBody)
            if (aiConfidence >= 0.6f && aiCategory != "UNKNOWN") {
                category = aiCategory
            } else {
                category = applyRuleBasedCategory(merchant, lowerBody, category)
            }

            // Subcategory Auto-detection
            subcategory = detectSubcategory(merchant, lowerBody)
        }

        // Handle specific type overrides based on final category
        if (category == "SELF_TRANSFER") {
            type = "SELF_TRANSFER"
        } else if (category == "CREDIT_CARD" || category == "CREDIT_CARD_SPEND" || category == "CREDIT_CARD_BILL" || category == "CREDIT_CARD_PAYMENT") {
            if (lowerBody.contains("card bill") || lowerBody.contains("card payment")) {
                type = "CREDIT_CARD_PAYMENT"
                category = "CREDIT_CARD"
            }
        } else if (category == "MANDATE") {
             if (isMandateCancelled) {
                 type = "MANDATE_CANCELLED"
                 category = "MANDATE_CANCELLED"
             } else if (isMandateCreated) {
                 type = "MANDATE_CREATED"
                 category = "MANDATE_CREATED"
             }
        } else if (category == "BILL" || category == "BILL_PENDING") {
             val isPaid = listOf("paid", "debited", "txn successful", "payment successful").any { lowerBody.contains(it) }
             if (isPaid) {
                 type = "EXPENSE"
                 category = "EXPENSE"
                 subcategory = "Bills"
             } else {
                 type = "BILL_PENDING"
                 category = "BILL_PENDING"
             }
        } else if (category == "VOUCHER" || category == "REWARD") {
             type = "REWARD"
             category = "REWARD"
        } else if (category == "RECHARGE") {
             type = "EXPENSE"
             category = "EXPENSE"
             subcategory = "Recharge"
        }

        return TransactionEntity(
            merchantName = merchant,
            amount = amount,
            type = type,
            category = category,
            subcategory = subcategory,
            date = timestamp,
            smsId = smsId,
            bankName = bankName,
            availableBalance = balance,
            rawMessage = messageBody,
            referenceId = referenceId
        )
    }

    private fun detectSubcategory(merchant: String, lowerBody: String): String {
        val lowerMerchant = merchant.lowercase()

        return when {
            listOf("swiggy", "zomato", "restaurant").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Food"
            listOf("bigbasket", "grocery", "dmart").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Groceries"
            listOf("netflix", "spotify", "prime", "hotstar").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Entertainment"
            listOf("act", "electricity", "bescom", "water bill", "broadband").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Bills"
            listOf("recharge", "data pack", "jio", "airtel", "vi").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Recharge"
            listOf("uber", "ola", "rapido", "namma metro", "irctc", "flight").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Travel"
            listOf("fuel", "petrol", "hpcl", "bpcl", "indian oil").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Transport"
            listOf("amazon", "flipkart", "myntra").any { lowerMerchant.contains(it) || lowerBody.contains(it) } -> "Shopping"
            else -> "Others"
        }
    }

    private fun applyRuleBasedCategory(merchant: String, lowerBody: String, defaultCategory: String): String {
        val lowerMerchant = merchant.lowercase()

        // 3. Investment
        if (listOf("fixed deposit", "fd", "recurring deposit", "rd", "deposit opened", "mutual fund", "sip").any { lowerBody.contains(it) }) {
            return "INVESTMENT"
        }

        // 4. Mandate
        if (listOf("upi mandate", "auto debit mandate", "emandate", "mandate created", "mandate cancelled", "mandate revoked", "mandate stopped").any { lowerBody.contains(it) }) {
            return "MANDATE"
        }

        // 5. Bill
        if (listOf("bill", "invoice", "amount due", "due date", "overdue", "pay before").any { lowerBody.contains(it) }) {
            return "BILL_PENDING"
        }

        // 5b. Recharge
        if (listOf("recharge successful", "data pack activated", "mobile recharge").any { lowerBody.contains(it) }) {
            return "RECHARGE"
        }

        // 6. Voucher
        if (listOf("voucher", "coupon", "reward", "cashback", "gold voucher").any { lowerBody.contains(it) }) {
            return "REWARD"
        }

        // 7. Subscription
        if (listOf("netflix", "amazon", "google", "spotify", "cloud", "internet").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return "SUBSCRIPTION"
        }

        // 8. Self transfer
        if (listOf("transfer between your accounts", "self transfer", "imps to own account").any { lowerBody.contains(it) }) {
            return "SELF_TRANSFER"
        }

        // 9. Credit Card
        if (listOf("card payment", "card bill").any { lowerBody.contains(it) }) {
            return "CREDIT_CARD_BILL"
        }
        if (listOf("credit card", "card ending").any { lowerBody.contains(it) }) {
            return "CREDIT_CARD_SPEND"
        }

        return defaultCategory
    }

    private fun extractReferenceId(body: String): String? {
        val matcher = Pattern.compile("(?i)(?:ref no|ref|txn id|transaction id|txn|reference)[\\s:-]*([A-Za-z0-9]+)").matcher(body)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun extractAmountAndType(body: String): Pair<Double, String>? {
        val lowerBody = body.lowercase()

        // Determine type based on keywords
        val isIncome = incomeKeywords.any { lowerBody.contains(it) }
        val isExpense = expenseKeywords.any { lowerBody.contains(it) }
        val isUpi = upiKeywords.any { lowerBody.contains(it) }
        val isVoucher = listOf("voucher", "coupon", "reward", "cashback").any { lowerBody.contains(it) }
        val isBill = listOf("bill", "invoice", "amount due", "due date", "overdue").any { lowerBody.contains(it) }
        val isRecharge = listOf("recharge").any { lowerBody.contains(it) }

        if (!isExpense && !isIncome && !isUpi && !isVoucher && !isBill && !isRecharge) {
            return null
        }

        val type = if (isIncome) {
            "INCOME"
        } else if (isExpense || isRecharge) {
            "EXPENSE"
        } else if (isVoucher) {
            "REWARD"
        } else if (isBill) {
            "BILL_PENDING"
        } else {
            "EXPENSE"
        }

        // Extract amount using the specified pattern
        val matcher = amountPattern.matcher(body)
        if (matcher.find()) {
            val amountRaw = matcher.group(1) ?: matcher.group(2)
            if (amountRaw != null) {
                val parsed = parseAmount(amountRaw)
                if (parsed > 0) {
                    return Pair(parsed, type)
                }
            }
        }

        // Secondary check for vouchers (e.g. "Voucher of 500", "Cashback of 500")
        if (isVoucher) {
            val voucherMatcher = java.util.regex.Pattern.compile("(?i)(?:voucher|cashback|reward).*?(?:rs\\.?|inr|of|for)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(body)
            if (voucherMatcher.find()) {
                val amountRaw = voucherMatcher.group(1)
                if (amountRaw != null) {
                    val parsed = parseAmount(amountRaw)
                    if (parsed > 0) {
                        return Pair(parsed, type)
                    }
                }
            }
        }

        return null
    }

    private fun parseAmount(amountStr: String?): Double {
        if (amountStr == null) return 0.0
        val cleanStr = amountStr.replace(",", "")
        return cleanStr.toDoubleOrNull() ?: 0.0
    }

    private fun extractMerchant(body: String, sender: String): String {
        val matcher = merchantPattern.matcher(body)
        if (matcher.find()) {
            var merchant = matcher.group(1)?.trim()
            if (!merchant.isNullOrBlank() && merchant.length > 2) {
                // Remove trailing UPI handles (e.g. "@oksbi", "-2@oksbi")
                val upiIndex = merchant.indexOf("@")
                if (upiIndex != -1) {
                    merchant = merchant.substring(0, upiIndex)
                    // Optional: remove trailing "-1", "-2" before the @ symbol if present
                    merchant = merchant.replace(Regex("-\\d+$"), "")
                }

                // Capitalize first letter
                return merchant.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
        return sender // Fallback to sender address if specific merchant is not found
    }

    private fun autoCategorize(merchant: String, body: String): String {
        val lowerMerchant = merchant.lowercase()
        val lowerBody = body.lowercase()

        return when {
            lowerMerchant.contains("amazon") -> "Shopping"
            lowerMerchant.contains("swiggy") || lowerMerchant.contains("zomato") -> "Food"
            lowerMerchant.contains("uber") || lowerMerchant.contains("ola") -> "Travel"
            lowerMerchant.contains("fuel") || lowerMerchant.contains("petrol") || lowerMerchant.contains("hpcl") || lowerMerchant.contains("bpcl") || lowerMerchant.contains("indian oil") -> "Transport"
            lowerMerchant.contains("atm") || lowerBody.contains("atm") -> "Cash Withdrawal"
            lowerMerchant.contains("salary") || lowerBody.contains("salary") -> "Income"
            lowerBody.contains("upi") && lowerBody.contains("received") -> "Income"
            else -> "General"
        }
    }

    private fun extractBankName(sender: String, body: String): String? {
        for ((bank, pattern) in bankPatterns) {
            if (pattern.matcher(sender).find() || pattern.matcher(body).find()) {
                return bank
            }
        }
        return null
    }

    private fun extractBalance(body: String): Double? {
        val matcher = balancePattern.matcher(body)
        if (matcher.find()) {
            return parseAmount(matcher.group(1))
        }
        return null
    }
}

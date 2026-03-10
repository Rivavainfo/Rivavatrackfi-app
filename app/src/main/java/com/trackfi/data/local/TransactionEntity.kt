package com.trackfi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String? = null,
    val merchantName: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val date: Long,
    val smsId: String? = null,
    val rawMessage: String? = null,
    val availableBalance: Double? = null,

    // Additional fields for new tracking features (Subscriptions, Vouchers, etc)
    val billingCycle: String? = null,
    val lastPaymentDate: Long? = null,
    val referenceId: String? = null,

    // Subcategory for granular tracking
    val subcategory: String? = null
)

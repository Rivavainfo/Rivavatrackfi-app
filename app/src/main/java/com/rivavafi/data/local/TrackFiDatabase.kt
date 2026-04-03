package com.rivavafi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, CategoryEntity::class, UserCorrectionEntity::class], version = 6, exportSchema = false)
abstract class TrackFiDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userCorrectionDao(): UserCorrectionDao
}

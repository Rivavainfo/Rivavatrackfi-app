package com.rivavafi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserCorrectionDao {
    @Query("SELECT * FROM user_corrections WHERE merchantName = :merchantName COLLATE NOCASE")
    suspend fun getCorrectionsForMerchant(merchantName: String): List<UserCorrectionEntity>

    @Query("SELECT * FROM user_corrections")
    suspend fun getAllCorrections(): List<UserCorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: UserCorrectionEntity)

    @Query("DELETE FROM user_corrections")
    suspend fun clearAllCorrections()
}

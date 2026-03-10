package com.trackfi.data.local

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import android.content.Context
import com.trackfi.data.preferences.UserPreferencesRepository
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTrackFiDatabase(app: Application): TrackFiDatabase {
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN billingCycle TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN lastPaymentDate INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN referenceId TEXT")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN subcategory TEXT")
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_corrections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `merchantName` TEXT NOT NULL, `keyword` TEXT, `category` TEXT NOT NULL, `subcategory` TEXT)")
            }
        }

        return Room.databaseBuilder(
            app,
            TrackFiDatabase::class.java,
            "trackfi_db"
        )
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: TrackFiDatabase): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: TrackFiDatabase): CategoryDao {
        return db.categoryDao()
    }

    @Provides
    @Singleton
    fun provideUserCorrectionDao(db: TrackFiDatabase): UserCorrectionDao {
        return db.userCorrectionDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(app: Application): UserPreferencesRepository {
        return UserPreferencesRepository(app.applicationContext)
    }
}

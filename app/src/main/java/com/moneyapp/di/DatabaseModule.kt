package com.moneyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.moneyapp.data.local.datastore.ThemePreferences
import com.moneyapp.data.local.db.AppDatabase
import com.moneyapp.data.local.db.dao.BackupMetadataDao
import com.moneyapp.data.local.db.dao.InvestmentDao
import com.moneyapp.data.local.db.dao.SavingDao
import com.moneyapp.data.local.db.dao.TransactionDao
import com.moneyapp.data.local.db.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "moneyapp_preferences"
)

/**
 * Hilt module yang menyediakan dependensi database, DAO, dan DataStore.
 *
 * Satisfies Requirements 15.1.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    // ── DAO providers ────────────────────────────────────────────────────────

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideInvestmentDao(db: AppDatabase): InvestmentDao = db.investmentDao()

    @Provides
    fun provideSavingDao(db: AppDatabase): SavingDao = db.savingDao()

    @Provides
    fun provideBackupMetadataDao(db: AppDatabase): BackupMetadataDao = db.backupMetadataDao()

    // ── DataStore ────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideThemePreferences(
        dataStore: DataStore<Preferences>
    ): ThemePreferences {
        return ThemePreferences(dataStore)
    }
}

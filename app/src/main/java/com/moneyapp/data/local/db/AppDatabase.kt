package com.moneyapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moneyapp.data.local.db.dao.BackupMetadataDao
import com.moneyapp.data.local.db.dao.InvestmentDao
import com.moneyapp.data.local.db.dao.SavingDao
import com.moneyapp.data.local.db.dao.TransactionDao
import com.moneyapp.data.local.db.dao.UserDao
import com.moneyapp.data.local.db.entity.BackupMetadataEntity
import com.moneyapp.data.local.db.entity.InvestmentEntity
import com.moneyapp.data.local.db.entity.SavingEntity
import com.moneyapp.data.local.db.entity.TransactionEntity
import com.moneyapp.data.local.db.entity.UserEntity

/**
 * Room database for the Money.me app.
 *
 * Contains all entities and exposes DAOs for data access.
 * 
 * Version History:
 * - Version 1: Initial schema
 * - Version 2: Added new columns to InvestmentEntity (average_price, current_price, 
 *   total_amount, is_sold, sold_date, sold_price)
 */
@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        InvestmentEntity::class,
        SavingEntity::class,
        BackupMetadataEntity::class
    ],
    version = 3,  // ← UPDATE: version 2 → 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun savingDao(): SavingDao
    abstract fun backupMetadataDao(): BackupMetadataDao

    companion object {
        const val DATABASE_NAME = "moneyapp.db"

        // Migration from version 1 to version 2
        // Adds new columns to investments table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for better investment tracking
                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN average_price REAL DEFAULT 0
                """)

                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN current_price REAL DEFAULT 0
                """)

                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN total_amount REAL DEFAULT 0
                """)

                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN is_sold INTEGER DEFAULT 0
                """)

                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN sold_date TEXT
                """)

                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN sold_price REAL DEFAULT 0
                """)

                // Update existing records: set is_sold = 0 for all existing investments
                database.execSQL("""
                    UPDATE investments SET is_sold = 0 WHERE is_sold IS NULL
                """)

                // If there's existing data, try to migrate initial_amount to total_amount
                // and current_value to current_price (if columns exist)
                try {
                    database.execSQL("""
                        UPDATE investments 
                        SET total_amount = initial_amount 
                        WHERE initial_amount IS NOT NULL
                    """)
                } catch (e: Exception) {
                    // initial_amount column might not exist, ignore
                }

                try {
                    database.execSQL("""
                        UPDATE investments 
                        SET current_price = current_value 
                        WHERE current_value IS NOT NULL
                    """)
                } catch (e: Exception) {
                    // current_value column might not exist, ignore
                }
            }
        }

        // Migration from version 2 to version 3
        // Adds created_at and updated_at columns to investments table
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN created_at TEXT DEFAULT ''
                """)
                database.execSQL("""
                    ALTER TABLE investments 
                    ADD COLUMN updated_at TEXT DEFAULT ''
                """)
            }
        }

        // Singleton instance
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the database instance with all migrations applied.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * For testing purposes only - get in-memory database
         */
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
}
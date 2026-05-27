package com.moneyapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
 * Version 1 — initial schema.
 */
@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        InvestmentEntity::class,
        SavingEntity::class,
        BackupMetadataEntity::class
    ],
    version = 1,
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
    }
}

package com.moneyapp.di

import com.moneyapp.data.repository.InvestmentRepositoryImpl
import com.moneyapp.data.repository.SavingRepositoryImpl
import com.moneyapp.data.repository.TransactionRepositoryImpl
import com.moneyapp.data.repository.UserRepositoryImpl
import com.moneyapp.domain.repository.InvestmentRepository
import com.moneyapp.domain.repository.SavingRepository
import com.moneyapp.domain.repository.TransactionRepository
import com.moneyapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module yang mengikat implementasi repository ke interface domain-nya.
 *
 * BackupRepositoryImpl akan ditambahkan di task 14.2.
 *
 * Satisfies Requirements 15.1.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindInvestmentRepository(
        impl: InvestmentRepositoryImpl
    ): InvestmentRepository

    @Binds
    @Singleton
    abstract fun bindSavingRepository(
        impl: SavingRepositoryImpl
    ): SavingRepository
}

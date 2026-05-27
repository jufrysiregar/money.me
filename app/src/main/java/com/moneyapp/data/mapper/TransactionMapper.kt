package com.moneyapp.data.mapper

import com.moneyapp.data.local.db.entity.TransactionEntity
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.model.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mapper between [TransactionEntity] (Room) and [Transaction] (domain model).
 *
 * - [TransactionType] enum is stored as a lowercase string ("income" / "expense") in the DB.
 * - [LocalDate] is stored as an ISO-8601 string in "yyyy-MM-dd" format.
 *
 * Satisfies Requirements 6.5.
 */
object TransactionMapper {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Converts a Room entity to a domain model. */
    fun toDomain(entity: TransactionEntity): Transaction = Transaction(
        id = entity.id,
        type = stringToTransactionType(entity.type),
        amount = entity.amount,
        category = entity.category,
        date = LocalDate.parse(entity.date, DATE_FORMATTER),
        note = entity.note,
        photoPath = entity.photoPath
    )

    /** Converts a domain model to a Room entity. */
    fun toEntity(domain: Transaction): TransactionEntity = TransactionEntity(
        id = domain.id,
        type = transactionTypeToString(domain.type),
        amount = domain.amount,
        category = domain.category,
        date = domain.date.format(DATE_FORMATTER),
        note = domain.note,
        photoPath = domain.photoPath
    )

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun stringToTransactionType(value: String): TransactionType =
        when (value.lowercase()) {
            "income" -> TransactionType.INCOME
            "expense" -> TransactionType.EXPENSE
            else -> throw IllegalArgumentException("Unknown transaction type: $value")
        }

    private fun transactionTypeToString(type: TransactionType): String =
        when (type) {
            TransactionType.INCOME -> "income"
            TransactionType.EXPENSE -> "expense"
        }
}

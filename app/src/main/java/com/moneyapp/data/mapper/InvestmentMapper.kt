package com.moneyapp.data.mapper

import com.moneyapp.data.local.db.entity.InvestmentEntity
import com.moneyapp.domain.model.Investment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mapper between [InvestmentEntity] (Room) and [Investment] (domain model).
 *
 * - [LocalDate] is stored as an ISO-8601 string in "yyyy-MM-dd" format.
 *
 * Satisfies Requirements 8.3.
 */
object InvestmentMapper {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Converts a Room entity to a domain model. */
    fun toDomain(entity: InvestmentEntity): Investment = Investment(
        id = entity.id,
        name = entity.name,
        amount = entity.amount,
        currentValue = entity.currentValue,
        date = LocalDate.parse(entity.date, DATE_FORMATTER)
    )

    /** Converts a domain model to a Room entity. */
    fun toEntity(domain: Investment): InvestmentEntity = InvestmentEntity(
        id = domain.id,
        name = domain.name,
        amount = domain.amount,
        currentValue = domain.currentValue,
        date = domain.date.format(DATE_FORMATTER)
    )
}

package com.moneyapp.data.mapper

import com.moneyapp.data.local.db.entity.SavingEntity
import com.moneyapp.domain.model.Saving
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mapper between [SavingEntity] (Room) and [Saving] (domain model).
 *
 * - [LocalDate] is stored as an ISO-8601 string in "yyyy-MM-dd" format.
 *
 * Satisfies Requirements 9.3.
 */
object SavingMapper {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Converts a Room entity to a domain model. */
    fun toDomain(entity: SavingEntity): Saving = Saving(
        id = entity.id,
        targetName = entity.targetName,
        targetAmount = entity.targetAmount,
        currentAmount = entity.currentAmount,
        targetDate = LocalDate.parse(entity.targetDate, DATE_FORMATTER)
    )

    /** Converts a domain model to a Room entity. */
    fun toEntity(domain: Saving): SavingEntity = SavingEntity(
        id = domain.id,
        targetName = domain.targetName,
        targetAmount = domain.targetAmount,
        currentAmount = domain.currentAmount,
        targetDate = domain.targetDate.format(DATE_FORMATTER)
    )
}

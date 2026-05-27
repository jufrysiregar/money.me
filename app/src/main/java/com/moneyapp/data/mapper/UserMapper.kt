package com.moneyapp.data.mapper

import com.moneyapp.data.local.db.entity.UserEntity
import com.moneyapp.domain.model.User

/**
 * Mapper between [UserEntity] (Room) and [User] (domain model).
 *
 * Satisfies Requirements 6.5.
 */
object UserMapper {

    /** Converts a Room entity to a domain model. */
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id,
        fullName = entity.fullName
    )

    /** Converts a domain model to a Room entity. */
    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        fullName = domain.fullName
    )
}

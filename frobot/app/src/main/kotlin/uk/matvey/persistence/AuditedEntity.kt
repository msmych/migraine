package uk.matvey.persistence

import java.time.Instant

abstract class AuditedEntity<ID : Entity.Id<*>>(
    override val id: ID,
    open val createdAt: Instant,
    open val updatedAt: Instant
) : Entity<ID>(id)

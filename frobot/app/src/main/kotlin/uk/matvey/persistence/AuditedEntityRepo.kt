package uk.matvey.persistence

import org.jooq.Condition
import org.jooq.OrderField
import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.DSL
import uk.matvey.persistence.JooqRepo.Companion.DEFAULT_LIMIT
import java.time.Clock
import java.time.Instant

abstract class AuditedEntityRepo<
    ID : Entity.Id<RID>,
    E : AuditedEntity<ID>,
    RID : Comparable<RID>,
    R : AuditedEntityRecord<RID, R>
    >(
    private val jooqRepo: JooqRepo,
    private val table: Table<R>,
    private val idField: TableField<R, RID>,
    private val updatedAtField: TableField<R, Instant>,
    private val clock: Clock = Clock.systemUTC()
) : EntityRepo<ID, E, RID, R>(jooqRepo, table, idField) {

    fun add(entity: E): E {
        val instant = clock.instant()
        return jooqRepo.add(table, entity.toRecord().setCreatedAt(instant).setUpdatedAt(instant)).toEntity()
    }

    fun update(entity: E): E {
        return jooqRepo.update(
            table,
            entity.toRecord().setUpdatedAt(clock.instant()),
            idField.eq(entity.id.value).and(updatedAtField.eq(entity.updatedAt))
        )
            .toEntity()
    }

    fun findAllWhere(
        condition: Condition = DSL.noCondition(),
        orderBy: List<OrderField<*>> = listOf(),
        limit: Int = DEFAULT_LIMIT
    ): Collection<E> {
        return jooqRepo.findAllWhere(table, condition, orderBy, limit).map { it.toEntity() }
    }
}

package uk.matvey.persistence

import org.jooq.Record
import org.jooq.Table
import org.jooq.TableField

abstract class EntityRepo<ID : Entity.Id<RID>, E : Entity<ID>, RID : Comparable<RID>, R : Record>(
    private val jooqRepo: JooqRepo,
    private val table: Table<R>,
    private val idField: TableField<R, RID>,
) {

    fun find(id: ID): E? {
        return jooqRepo.findOneWhere(table, idField.eq(id.value))?.toEntity()
    }

    fun get(id: ID): E {
        return find(id) ?: throw IllegalArgumentException("Not found ${table.name} [id = $id]")
    }


    abstract fun E.toRecord(): R

    abstract fun R.toEntity(): E
}

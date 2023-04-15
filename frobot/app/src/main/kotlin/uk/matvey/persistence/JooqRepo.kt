package uk.matvey.persistence

import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.OrderField
import org.jooq.Query
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.ParamType
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.Connection
import javax.sql.DataSource

class JooqRepo(
    private val dataSource: DataSource,
    private val sqlDialect: SQLDialect = SQLDialect.POSTGRES) {

    fun <R : Record> add(table: Table<R>, record: R): R {
        return withConnection { conn ->
            prepareQuery(conn) { dslCtx ->
                val query = dslCtx.insertQuery(table)
                query.addRecord(record)
                query.setReturning()
                query
            }.coerce(table).fetchSingle()
        }
    }

    fun <R : Record> update(table: Table<R>, record: R, condition: Condition = DSL.noCondition()): R {
        return withConnection { conn ->
            prepareQuery(conn) { dslCtx ->
                val query = dslCtx.updateQuery(table)
                query.setRecord(record)
                query.addConditions(condition)
                query.setReturning()
                query
            }.coerce(table).fetchSingle()
        }
    }

    fun <R : Record> findAllWhere(
        table: Table<R>,
        condition: Condition,
        orderBy: List<OrderField<*>> = listOf(),
        limit: Int = DEFAULT_LIMIT
    ): Collection<R> {
        return withConnection { conn ->
            prepareQuery(conn) { dslCtx ->
                dslCtx.select(table.fields().toList()).from(table).where(condition).orderBy(orderBy).limit(limit)
            }.coerce(table).fetch()
        }
    }

    fun <R : Record> findOneWhere(table: Table<R>, condition: Condition): R? {
        return withConnection { conn ->
            prepareQuery(conn) { dslCtx ->
                dslCtx.select(table.fields().toList()).from(table).where(condition)
            }.coerce(table).fetchOne()
        }
    }

    fun <R> withConnection(block: (Connection) -> R): R {
        return dataSource.connection.use(block)
    }

    fun prepareQuery(connection: Connection, queryBuilder: (DSLContext) -> Query): ResultQuery<Record> {
        val dslCtx = DSL.using(connection, sqlDialect, JOOQ_SETTINGS)
        val query = queryBuilder(dslCtx)
        return dslCtx.resultQuery(
            dslCtx.render(query),
            *dslCtx.extractParams(query).values.filterNot { it.isInline }.toTypedArray()
        )
    }

    companion object {
        const val DEFAULT_LIMIT = 500
        private val JOOQ_SETTINGS = Settings().withRenderNamedParamPrefix("$").withParamType(ParamType.NAMED)
    }
}

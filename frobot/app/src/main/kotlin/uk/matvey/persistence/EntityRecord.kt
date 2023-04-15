package uk.matvey.persistence

import org.jooq.Record

interface EntityRecord<ID : Comparable<ID>, R : Record> : Record {

    fun getId(): ID

    fun setId(id: ID): R
}

package uk.matvey.persistence

import org.jooq.Record
import java.time.Instant

interface AuditedEntityRecord<ID : Comparable<ID>, R : Record> : EntityRecord<ID, R>, Record {

    fun getCreatedAt(): Instant

    fun setCreatedAt(date: Instant): R

    fun getUpdatedAt(): Instant

    fun setUpdatedAt(date: Instant): R
}

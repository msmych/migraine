package uk.matvey.persistence

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

abstract class Entity<ID : Entity.Id<*>>(
    open val id: ID
) {

    open class Id<ID : Comparable<ID>>(open val value: ID)

    fun idJson() = buildJsonObject {
        put("id", id.value.toString())
    }
}

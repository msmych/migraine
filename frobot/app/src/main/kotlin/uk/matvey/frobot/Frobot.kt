package uk.matvey.frobot

import uk.matvey.frobot.Frobot.Id
import uk.matvey.frobot.Frobot.State.BATTERY_LOW
import uk.matvey.persistence.AuditedEntity
import uk.matvey.persistence.Entity
import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

data class Frobot(
    override val id: Id,
    val userId: Long,
    val state: State,
    val rockGardenMessageId: Int?,
    val rockGardenBoard: RockGardenBoard?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : AuditedEntity<Id>(id, createdAt, updatedAt) {

    class Id(override val value: UUID) : Entity.Id<UUID>(value)

    enum class State {
        BATTERY_LOW,
        ACTIVE,
        OVERHEATED,
    }

    fun rockGardenBoard() = requireNotNull(rockGardenBoard)

    companion object {

        fun frobot(userId: Long): Frobot {
            val id = Id(randomUUID())
            val now = now()
            return Frobot(id, userId, BATTERY_LOW, null, null, now, now)
        }
    }
}

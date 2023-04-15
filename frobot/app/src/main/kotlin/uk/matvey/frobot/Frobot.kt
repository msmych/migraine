package uk.matvey.frobot

import uk.matvey.persistence.AuditedEntity
import uk.matvey.persistence.Entity
import java.time.Instant
import java.util.UUID

data class Frobot(
    override val id: Id,
    val userId: Long,
    val batteryLevel: BatteryLevel,
    val lotusPondMessageId: Int?,
    val lotusPondBoard: LotusPondBoard?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : AuditedEntity<Frobot.Id>(id, createdAt, updatedAt) {

    class Id(override val value: UUID) : Entity.Id<UUID>(value)

    enum class BatteryLevel {
        LOW,
        HIGH
    }

    fun fireRocksBoard() = requireNotNull(lotusPondBoard)
}

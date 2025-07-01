package uk.matvey.frobot

import uk.matvey.frobot.Frobot.State.BATTERY_LOW
import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

data class Frobot(
    val id: UUID,
    val userId: Long,
    val state: State,
    val rockGardenMessageId: Int?,
    val rockGardenBoard: RockGardenBoard?,
    val createdAt: Instant,
    val updatedAt: Instant,
){

    enum class State {
        BATTERY_LOW,
        ACTIVE,
        OVERHEATED,
    }

    fun rockGardenBoard() = requireNotNull(rockGardenBoard)

    companion object {

        fun frobot(userId: Long): Frobot {
            val id = randomUUID()
            val now = now()
            return Frobot(id, userId, BATTERY_LOW, null, null, now, now)
        }
    }
}

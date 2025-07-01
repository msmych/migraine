package uk.matvey.frobot

import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.SuspendingConnection
import java.time.ZoneOffset.UTC
import java.util.UUID

class FrobotRepository(
    private val db: SuspendingConnection,
) {

    suspend fun findByUserId(userId: Long): Frobot? {
        return db.sendPreparedStatement(
            """
            select * from frobot where user_id = ?
        """.trimIndent(),
            listOf(userId)
        ).rows.singleOrNull()?.let { toFrobot(it) }
    }

    suspend fun add(frobot: Frobot): Frobot {
        val row = db.sendPreparedStatement(
            """
            insert into frobot (id, user_id, state, rock_garden_message_id, rock_garden_board, created_at, updated_at)
            values (?, ?, ?, ?, ?, now(), now())
            returning *
        """.trimIndent(),
            listOf(
                frobot.id,
                frobot.userId,
                frobot.state.name,
                frobot.rockGardenMessageId,
                frobot.rockGardenBoard?.toString()
            )
        ).rows.single()
        return toFrobot(row)
    }

    suspend fun update(
        id: UUID,
        state: Frobot.State? = null,
        rockGardenMessageId: Int? = null,
        rockGardenBoard: RockGardenBoard? = null,
    ): Frobot {
        val map = listOf(
            "state" to state?.name,
            "rock_garden_message_id" to rockGardenMessageId,
            "rock_garden_board" to rockGardenBoard?.serialize()
        )
            .mapNotNull { (k, v) -> v?.let { k to it } }
            .toMap()
        val sets = map.keys.joinToString(", ") { "$it = ?" }
        val row = db.sendPreparedStatement(
            """
            update frobot
            set $sets, updated_at = now()
            where id = ?
            returning *
        """.trimIndent(),
            map.values + id
        ).rows.single()
        return toFrobot(row)
    }

    private fun toFrobot(row: RowData): Frobot {
        return Frobot(
            id = row.getAs("id"),
            userId = requireNotNull(row.getLong("user_id")),
            state = Frobot.State.valueOf(requireNotNull(row.getString("state"))),
            rockGardenMessageId = row.getLong("rock_garden_message_id")?.toInt(),
            rockGardenBoard = row.getString("rock_garden_board")?.let(RockGardenBoard::fromString),
            createdAt = requireNotNull(row.getDate("created_at")).toInstant(UTC),
            updatedAt = requireNotNull(row.getDate("updated_at")).toInstant(UTC),
        )
    }
}
package uk.matvey.frobot

import com.sun.net.httpserver.HttpServer
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import uk.matvey.frobot.Constants.ELECTRICITY
import uk.matvey.frobot.Constants.INSECTS
import uk.matvey.frobot.Constants.NULL_POINTER_MESSAGES
import uk.matvey.frobot.Frobot.Companion.frobot
import uk.matvey.frobot.Frobot.State.ACTIVE
import uk.matvey.frobot.Frobot.State.BATTERY_LOW
import uk.matvey.frobot.Frobot.State.OVERHEATED
import uk.matvey.frobot.RockGardenCell.TreasureMap
import uk.matvey.persistence.JooqRepo
import uk.matvey.telek.Bot
import uk.matvey.telek.ParseMode
import java.net.InetSocketAddress
import java.util.concurrent.ThreadLocalRandom

private val log = KotlinLogging.logger {}

fun main() {
    val frobotDbUser = System.getenv("FROBOT_DB_USER")
    val frobotDbPassword = System.getenv("FROBOT_DB_PASSWORD")
    val frobotDbName = System.getenv("FROBOT_DB_NAME")
    val frobotDbHost = System.getenv("FROBOT_DB_HOST")

    val bot = Bot(System.getenv("FROBOT_TG_BOT_TOKEN"))

    val jooqRepo = JooqRepo(
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$frobotDbHost/$frobotDbName"
            username = frobotDbUser
            password = frobotDbPassword
            driverClassName = "org.postgresql.Driver"
        })
    )
    val frobotRepo = FrobotRepo(jooqRepo)

    log.info { "Starting Frobot server on port 10000" }
    val server = HttpServer.create(InetSocketAddress(10000), 0)
    server.createContext("/health") { exchange ->
        exchange.sendResponseHeaders(200, 0)
        exchange.responseBody.use { it.write("OK".toByteArray()) }
    }
    server.executor = null
    server.start()
    log.info { "Frobot server started on port 10000" }

    CoroutineScope(Dispatchers.IO).launch {
        bot.start { update ->
            try {
                val userId = if (update.message != null) {
                    update.message().from().id
                } else {
                    update.callbackQuery().from.id
                }

                val frobot = frobotRepo.findBy(userId) ?: frobotRepo.add(frobot(userId))
                when (frobot.state) {
                    BATTERY_LOW -> {
                        if (update.message().text in INSECTS) {
                            frobotRepo.update(frobot.copy(state = ACTIVE))
                            bot.sendMessage(userId, "🐸 Yummy!")
                            bot.sendMessage(userId, "🔋")
                        } else if (update.message().text in ELECTRICITY) {
                            bot.sendMessage(userId, "🐸 Not tasty")
                            bot.sendMessage(userId, "🪫")
                        } else {
                            bot.sendMessage(userId, "🪫")
                        }
                    }
                    ACTIVE -> {
                        if (update.message().text == "/jump") {
                            frobot.rockGardenMessageId?.let { messageId ->
                                bot.editMessageInlineKeyboard(update.message(), listOf())
                                bot.editMessage(update.message(), "🧯")
                            }
                            val initialBoard = RockGardenBoard.fromString(
                                """
                            brrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                            rrrrrrrr
                        """.trimIndent().replace("\n", "")
                            )
                            val sendMessageResult = bot.sendMessage(userId, "🐸 Wow, what a beautiful rock garden\\!", parseMode = ParseMode.MarkdownV2)
                            frobotRepo.update(
                                frobot.copy(
                                    rockGardenMessageId = sendMessageResult.messageId().messageId,
                                    rockGardenBoard = initialBoard
                                )
                            )
                        } else if (update.callbackQuery() != null) {
                            val (i, j) = update.callbackQuery().data().let { it[0].digitToInt() to it[1].digitToInt() }
                            val message = update.callbackQuery().message()
                            if (frobot.rockGardenBoard().cellAt(i, j) is TreasureMap && frobot.rockGardenBoard()
                                    .isReachableRock(i, j)
                            ) {
                                frobotRepo.update(frobot.copy(state = OVERHEATED))
                                bot.sendMessage(userId, "☠️ *OVERHEATED*", parseMode = ParseMode.MarkdownV2)
                                bot.sendMessage(userId, "☠️ *ALL SYSTEMS DOWN*", parseMode = ParseMode.MarkdownV2)
                                bot.sendMessage(userId, "🤖 JUNK Robotics™®©: rescue team is on its way")
                                bot.sendMessage(userId, "🔵🔵🔴🟢")
                            } else {
                                val updatedBoard = frobot.rockGardenBoard().move(i, j)
                                if (updatedBoard != frobot.rockGardenBoard) {
                                    frobotRepo.update(frobot.copy(rockGardenBoard = updatedBoard))
                                    bot.editMessageInlineKeyboard(message, updatedBoard.toInlineKeyboard())
                                    when (updatedBoard.serialize().count { it == 'f' }) {
                                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> null
                                        12 -> " Hmm, starting to feel a little toasty in here"
                                        28 -> "❗️ Okay, this is getting seriously hot"
                                        48 -> "❗️ Oh man, I'm burning up"
                                        56 -> "⚠️ Language module квакнулся. 당신은 마주칠 수도 있습니다 alcuni problemi स्थानीयकरण के"
                                        60 -> "‼️️ Danger ‼️ Critical overheat"
                                        62 -> " Oh look! There's a map over there!"
                                        else -> "⚠️ ${NULL_POINTER_MESSAGES.random()}"
                                            .takeIf { ThreadLocalRandom.current().nextInt() % 24 == 0 }
                                    }?.let { logMessage ->
                                        bot.editMessage(message, "${message.text()}\n🐸$logMessage".replace("!", "\\!")
                                            .replace(".", "\\."), updatedBoard.toInlineKeyboard())
                                    }
                                }
                                bot.answerCallbackQuery(update.callbackQuery().id)
                            }
                        }
                    }
                    OVERHEATED -> {}
                }
            } catch (e: Exception) {
                log.error(e) { "Failed to process $update" }
            }
        }
    }
}

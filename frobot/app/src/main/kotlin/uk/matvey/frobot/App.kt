package uk.matvey.frobot

import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder.createConnectionPool
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.flywaydb.core.Flyway.configure
import uk.matvey.frobot.Constants.ELECTRICITY
import uk.matvey.frobot.Constants.INSECTS
import uk.matvey.frobot.Constants.NULL_POINTER_MESSAGES
import uk.matvey.frobot.Frobot.Companion.frobot
import uk.matvey.frobot.Frobot.State.ACTIVE
import uk.matvey.frobot.Frobot.State.BATTERY_LOW
import uk.matvey.frobot.Frobot.State.OVERHEATED
import uk.matvey.frobot.RockGardenCell.TreasureMap
import uk.matvey.telek.Bot
import uk.matvey.telek.Message
import uk.matvey.telek.ParseMode
import uk.matvey.telek.RequestException
import java.util.concurrent.ThreadLocalRandom

private val log = KotlinLogging.logger {}

fun main() {
    val bot = Bot(
        token = System.getenv("FROBOT_TG_BOT_TOKEN"),
        defaultParseMode = ParseMode.MarkdownV2,
    )

    val dbUrl = System.getenv("FROBOT_DB_URL")
    val dbUsername = System.getenv("FROBOT_DB_USERNAME")
    val dbPassword = System.getenv("FROBOT_DB_PASSWORD")
    val db = createConnectionPool(
        dbUrl
    ) {
        username = dbUsername
        password = dbPassword
    }.asSuspending
    configure()
        .dataSource(dbUrl, dbUsername, dbPassword)
        .locations("classpath:db/migration")
        .load()
        .migrate()
    val frobotRepository = FrobotRepository(db)


    startServer()

    runBlocking {
        bot.start { update ->
            try {
                val userId = if (update.message != null) {
                    update.message().from().id
                } else {
                    update.callbackQuery().from.id
                }

                val frobot = frobotRepository.findByUserId(userId) ?: frobotRepository.add(frobot(userId))
                when (frobot.state) {
                    BATTERY_LOW -> {
                        if (update.message?.text in INSECTS) {
                            frobotRepository.update(frobot.id, state = ACTIVE)
                            bot.sendMessage(userId, "🐸 Yummy!")
                            bot.sendMessage(userId, "🔋")
                        } else if (update.message?.text in ELECTRICITY) {
                            bot.sendMessage(userId, "🐸 Not tasty")
                            bot.sendMessage(userId, "🪫")
                        } else {
                            bot.sendMessage(userId, "🪫")
                        }
                    }
                    ACTIVE -> {
                        if (update.message?.text == "/jump") {
                            frobot.rockGardenMessageId?.let { messageId ->
                                try {
                                    bot.editMessage(
                                        messageId = Message.Id(update.message().chat.chatId(), messageId),
                                        text = "🧯",
                                        inlineKeyboard = listOf()
                                    )
                                } catch (e: RequestException) {
                                    if (e.message !in setOf(
                                            MESSAGE_TO_EDIT_NOT_FOUND,
                                            MESSAGE_IS_NOT_MODIFIED_EXCEPTION
                                        )
                                    ) {
                                        throw e
                                    }
                                }
                            }
                            val initialBoard = RockGardenBoard.initial()
                            val sendMessageResult = bot.sendMessage(
                                userId,
                                "🐸 Wow, what a beautiful rock garden!",
                                inlineKeyboard = initialBoard.toInlineKeyboard()
                            )
                            frobotRepository.update(
                                id = frobot.id,
                                rockGardenMessageId = sendMessageResult.messageId().messageId,
                                rockGardenBoard = initialBoard
                            )
                        } else if (update.callbackQuery != null) {
                            val (i, j) = update.callbackQuery().data().let { it[0].digitToInt() to it[1].digitToInt() }
                            val message = update.callbackQuery().message()
                            if (frobot.rockGardenBoard().cellAt(i, j) is TreasureMap && frobot.rockGardenBoard()
                                    .isReachableRock(i, j)
                            ) {
                                frobotRepository.update(frobot.id, state = OVERHEATED)
                                bot.sendMessage(userId, "☠️ *OVERHEATED*")
                                bot.sendMessage(userId, "☠️ *ALL SYSTEMS DOWN*")
                                bot.sendMessage(userId, "🤖 JUNK Robotics™®©: rescue team is on its way")
                                bot.sendMessage(userId, "🔵🔵🔴🟢")
                            } else {
                                val updatedBoard = frobot.rockGardenBoard().move(i, j)
                                if (updatedBoard != frobot.rockGardenBoard) {
                                    frobotRepository.update(frobot.id, rockGardenBoard = updatedBoard)
                                    bot.editMessageInlineKeyboard(
                                        update.callbackQuery().message().messageId(),
                                        updatedBoard.toInlineKeyboard()
                                    )
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
                                        bot.editMessage(
                                            update.callbackQuery().message().messageId(),
                                            "${message.text()}\n🐸$logMessage",
                                            inlineKeyboard = updatedBoard.toInlineKeyboard()
                                        )
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

private fun startServer() {
    log.info { "Starting Frobot server on port 10000" }
    embeddedServer(factory = Netty, port = 10000) {
        routing {
            route("/health") {
                get {
                    call.respond(OK, "\uD83D\uDC38 Frobot is alive and kicking!")
                }
            }
        }
    }.start(
        wait = false
    )
    log.info { "Frobot server started on port 10000" }
}

private const val MESSAGE_TO_EDIT_NOT_FOUND = "Bad Request: message to edit not found"
private const val MESSAGE_IS_NOT_MODIFIED_EXCEPTION =
    "Bad Request: message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message"


package uk.matvey.frobot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode.MarkdownV2
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.EditMessageReplyMarkup
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import uk.matvey.frobot.Frobot.Companion.frobot
import uk.matvey.frobot.Frobot.State.ACTIVE
import uk.matvey.frobot.Frobot.State.BATTERY_LOW
import uk.matvey.frobot.Frobot.State.OVERHEATED
import uk.matvey.frobot.RockGardenCell.TreasureMap
import uk.matvey.frobot.TelegramBotSupport.messageText
import uk.matvey.frobot.TelegramBotSupport.user
import uk.matvey.persistence.JooqRepo
import java.util.concurrent.ThreadLocalRandom

private val INSECTS = setOf("🦋", "🐝", "🐞", "🐜", "🦟", "🪰")
private val NULL_POINTER_MESSAGES = setOf(
    "Null Pointer Disaster",
    "Null Pointer Catastrophe",
    "Epic Null Pointer Fail",
    "Null Pointer Misery",
    "Null Pointer Trouble",
    "Null Pointer Death",
    "Null Pointer Explosion",
    "Null Pointer Fiasco",
)

private val log = KotlinLogging.logger {}

fun main() {
    val frobotDbUser = System.getenv("FROBOT_DB_USER")
    val frobotDbPassword = System.getenv("FROBOT_DB_PASSWORD")
    val frobotDbName = System.getenv("FROBOT_DB_NAME")
    val frobotDbHost = System.getenv("FROBOT_DB_HOST")

    val bot = TelegramBot(System.getenv("FROBOT_TG_BOT_TOKEN"))

    val jooqRepo = JooqRepo(
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$frobotDbHost/$frobotDbName"
            username = frobotDbUser
            password = frobotDbPassword
            driverClassName = "org.postgresql.Driver"
        })
    )
    val frobotRepo = FrobotRepo(jooqRepo)

    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            log.info { update }
            try {
                val userId = update.user().id()

                val frobot = frobotRepo.findBy(userId) ?: frobotRepo.add(frobot(userId))
                when (frobot.state) {
                    BATTERY_LOW -> {
                        if (update.messageText() in INSECTS) {
                            frobotRepo.update(frobot.copy(state = ACTIVE))
                            bot.execute(SendMessage(userId, "🐸 Yummy!"))
                            bot.execute(SendMessage(userId, "🔋"))
                        } else {
                            bot.execute(SendMessage(userId, "🪫"))
                        }
                    }
                    ACTIVE -> {
                        if (update.messageText() == "/jump") {
                            frobot.rockGardenMessageId?.let { messageId ->
                                bot.execute(EditMessageReplyMarkup(userId, messageId).replyMarkup(InlineKeyboardMarkup()))
                                bot.execute(EditMessageText(userId, messageId, "🧯"))
                            }
                            val initialBoard = RockGardenBoard.fromString("""
                                brrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                                rrrrrrrr
                            """.trimIndent().replace("\n", ""))
                            val result = bot.execute(SendMessage(userId, "🐸 What a beautiful rock garden\\!")
                                .replyMarkup(initialBoard.toInlineKeyboard()).parseMode(MarkdownV2))
                            frobotRepo.update(frobot.copy(rockGardenMessageId = result.message().messageId(), rockGardenBoard = initialBoard))
                        } else if (update.callbackQuery() != null) {
                            val (i, j) = update.callbackQuery().data().let { it[0].digitToInt() to it[1].digitToInt() }
                            val message = update.callbackQuery().message()
                            if (frobot.rockGardenBoard().cellAt(i, j) is TreasureMap && frobot.rockGardenBoard().isReachableRock(i, j)) {
                                frobotRepo.update(frobot.copy(state = OVERHEATED))
                                bot.execute(SendMessage(userId, "☠️ *OVERHEATED*").parseMode(MarkdownV2))
                                bot.execute(SendMessage(userId, "☠️ *ALL SYSTEMS DOWN*").parseMode(MarkdownV2))
                                bot.execute(SendMessage(userId, "🤖 JUNK Robotics: rescue team is on their way"))
                                bot.execute(SendMessage(userId, "🔵🔵🔴🟢"))
                            } else {
                                val updatedBoard = frobot.rockGardenBoard().move(i, j)
                                if (updatedBoard != frobot.rockGardenBoard) {
                                    frobotRepo.update(frobot.copy(rockGardenBoard = updatedBoard))
                                    bot.execute(EditMessageReplyMarkup(userId, message.messageId())
                                        .replyMarkup(updatedBoard.toInlineKeyboard()))
                                    when (updatedBoard.serialize().count { it == 'f' }) {
                                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> null
                                        12 -> " Huh it's getting kinda hot here"
                                        28 -> "❗️ Seriously, it's too hot here"
                                        48 -> "❗️ Man it's hot"
                                        56 -> "⚠️ Pozor! Language module квакнулся. La localizzazione potrebbe 멈추다"
                                        60 -> "‼️️ Danger ‼️ Critical overheat"
                                        62 -> " Oh look! There's a map over there!"
                                        else -> "⚠️ ${NULL_POINTER_MESSAGES.random()}"
                                            .takeIf { ThreadLocalRandom.current().nextInt() % 24 == 0 }
                                    }?.let { logMessage ->
                                        bot.execute(EditMessageText(userId, message.messageId(), "${message.text()}\n🐸$logMessage".replace("!", "\\!"))
                                            .replyMarkup(updatedBoard.toInlineKeyboard())
                                            .parseMode(MarkdownV2))
                                    }
                                }
                                bot.execute(AnswerCallbackQuery(update.callbackQuery().id()))
                            }
                        }
                    }
                    OVERHEATED -> {}
                }
            } catch (e: Exception) {
                log.error(e) { "Failed to process $update" }
            }
        }
        CONFIRMED_UPDATES_ALL
    }
}

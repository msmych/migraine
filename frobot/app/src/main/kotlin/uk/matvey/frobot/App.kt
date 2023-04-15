package uk.matvey.frobot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.EditMessageReplyMarkup
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import uk.matvey.frobot.Frobot.BatteryLevel.HIGH
import uk.matvey.frobot.Frobot.BatteryLevel.LOW
import uk.matvey.frobot.FrobotState.BATTERY_LOW
import uk.matvey.frobot.FrobotState.FIRE_ROCKS
import uk.matvey.frobot.TelegramBotSupport.messageText
import uk.matvey.frobot.TelegramBotSupport.user
import uk.matvey.persistence.JooqRepo
import java.time.Instant.now
import java.util.UUID.randomUUID

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    println(App().greeting)

    val bot = TelegramBot("")

    val jooqRepo = JooqRepo(
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:55000/postgres"
            username = "postgres"
            password = "postgres"
            driverClassName = "org.postgresql.Driver"
        })
    )
    val frobotRepo = FrobotRepo(jooqRepo)
    val frobotStateResolver = FrobotStateResolver()

    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            println(update)
            try {
                val userId = update.user().id()

                val frobot = frobotRepo.findBy(userId) ?: frobotRepo.add(Frobot(Frobot.Id(randomUUID()), userId, LOW, null, null, now(), now()))
                when (frobotStateResolver.resolveState(frobot)) {
                    BATTERY_LOW -> {
                        if (update.messageText() == "🦟") {
                            frobotRepo.update(frobot.copy(batteryLevel = HIGH))
                            bot.execute(SendMessage(userId, "🐸 Yummy!"))
                            bot.execute(SendMessage(userId, "🔋"))
                        } else {
                            bot.execute(SendMessage(userId, "🪫"))
                        }
                    }
                    FIRE_ROCKS -> {
                        if (update.messageText() == "/jump") {
                            frobot.lotusPondMessageId?.let { messageId ->
                                bot.execute(EditMessageReplyMarkup(userId, messageId).replyMarkup(InlineKeyboardMarkup()))
                                bot.execute(EditMessageText(userId, messageId, "🔥"))
                            }
                            val initialBoard = LotusPondBoard.fromString("""
                                blllllll
                                llllllll
                                llllllll
                                llllllll
                                llllllll
                                llllllll
                                llllllll
                                llllllll
                            """.trimIndent().replace("\n", ""))
                            val result = bot.execute(SendMessage(userId, "🐸 Let's go!")
                                .replyMarkup(initialBoard.toInlineKeyboard()))
                            frobotRepo.update(frobot.copy(lotusPondMessageId = result.message().messageId(), lotusPondBoard = initialBoard))
                        } else if (update.callbackQuery() != null) {
                            val data = update.callbackQuery().data()
                            val updatedBoard = frobot.fireRocksBoard().move(data[0].digitToInt(), data[1].digitToInt())
                            frobotRepo.update(frobot.copy(lotusPondBoard = updatedBoard))
                            bot.execute(EditMessageReplyMarkup(userId, update.callbackQuery().message().messageId())
                                .replyMarkup(updatedBoard.toInlineKeyboard()))
                            bot.execute(AnswerCallbackQuery(update.callbackQuery().id()))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        CONFIRMED_UPDATES_ALL
    }
}

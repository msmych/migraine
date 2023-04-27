package uk.matvey.frobot

import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User

object TelegramBotUpdateSupport {

    fun Update.user(): User {
        return if (message() != null) {
            message().from()
        } else {
            callbackQuery().from()
        }
    }

    fun Update.messageText(): String? {
        val message = message() ?: return null
        return message.text()
    }
}

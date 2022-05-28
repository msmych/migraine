package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import com.pengrad.telegrambot.request.SendSticker
import com.pengrad.telegrambot.response.SendResponse

fun Update.from(): User {
    return if (message() != null) {
        message().from()
    } else {
        callbackQuery().from()
    }
}

fun TelegramBot.sendMessage(user: User, text: String): SendResponse {
    return execute(SendMessage(user.id(), text))
}

fun TelegramBot.sendSticker(user: User, fileId: String): SendResponse {
    return execute(SendSticker(user.id(), fileId))
}

fun TelegramBot.sendPhoto(
    user: User,
    photo: ByteArray,
    caption: String? = null,
    markup: Keyboard? = null
): SendResponse {
    var request = SendPhoto(user.id(), photo)
    if (caption != null) {
        request = request.caption(caption)
    }
    if (markup != null) {
        request = request.replyMarkup(markup)
    }
    return execute(request)
}
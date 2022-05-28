package bot

import bot.State.FINAL
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update

object GotDessertTransition : State.Transition {

    override fun next(update: Update, bot: TelegramBot): State? {
        val message = update.message() ?: return null
        if (message.location() == null) {
            return null
        }
        bot.sendMessage(update.from(), "To be continued...")
        return FINAL
    }

}
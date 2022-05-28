package bot

import bot.AppSupport.resource
import bot.State.AT_THE_ICE_CREAM_TRUCK
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup

object OnTheStreetTransition : State.Transition {

    override fun next(update: Update, bot: TelegramBot): State? {
        if (update.message()?.text() != "/go") {
            return null
        }
        bot.sendPhoto(
            update.from(),
            resource("images/ice-cream-truck-view.png").readBytes(),
            "Что берем?",
            ReplyKeyboardMarkup(*DESSERT_IMAGES.keys.toTypedArray())
        )
        return AT_THE_ICE_CREAM_TRUCK
    }

}

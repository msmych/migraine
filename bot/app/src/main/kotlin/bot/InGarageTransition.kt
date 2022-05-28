package bot

import bot.AppSupport.resource
import bot.State.ON_THE_STREET
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update

object InGarageTransition : State.Transition {

    override fun next(update: Update, bot: TelegramBot): State? {
        if (update.message()?.text() != "/go") {
            return null
        }
        bot.sendPhoto(update.from(), resource("images/street-view.png").readBytes())
        return ON_THE_STREET
    }
}
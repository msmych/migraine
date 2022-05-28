package bot

import bot.AppSupport.resource
import bot.State.IN_GARAGE
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update

object SleepingTransition : State.Transition {

    private val sleepingStickers = setOf(
        "CAACAgIAAxkBAAMJYokMiQn64ICbiBlo-wjOgMLW-xIAAgsAA8A2TxMI-K_YWTjJMyQE",
        "CAACAgIAAxkBAAMLYokNpMIuKzIQ-95FEbfepdmSOusAAnMAA_cCyA-3eTFursKAuSQE"
    )

    override fun next(update: Update, bot: TelegramBot): State? {
        if (update.message()?.sticker()?.emoji() != "⏰") {
            bot.sendSticker(update.from(), sleepingStickers.random())
            return null
        }
        bot.sendPhoto(update.from(), resource("images/garage-view.png").readBytes())
        return IN_GARAGE
    }
}
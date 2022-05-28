package bot

import bot.AppSupport.resource
import bot.State.GOT_DESSERT
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove

val DESSERT_IMAGES = mapOf(
    "\uD83C\uDF66" to "images/ice-cream-truck-view-ice-cream.png",
    "\uD83E\uDD50" to "images/ice-cream-truck-view-croissant.png",
    "\uD83C\uDF6C" to "images/ice-cream-truck-view-candy.png"
)

object AtTheIceCreamTruckTransition : State.Transition {

    override fun next(update: Update, bot: TelegramBot): State? {
        val text = update.message()?.text()
        if (!DESSERT_IMAGES.containsKey(text)) {
            return null
        }
        bot.sendPhoto(
            update.from(),
            resource(requireNotNull(DESSERT_IMAGES[text])).readBytes(),
            "Отличный выбор! Куда доставить?",
            ReplyKeyboardRemove()
        )
        return GOT_DESSERT
    }
}
package bot

import bot.AppSupport.resource
import bot.State.IN_GARAGE
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update

object SleepingTransition : State.Transition {

    private val sleepingStickers = setOf(
        "CAACAgIAAxkBAAMJYokMiQn64ICbiBlo-wjOgMLW-xIAAgsAA8A2TxMI-K_YWTjJMyQE",
        "CAACAgIAAxkBAAMLYokNpMIuKzIQ-95FEbfepdmSOusAAnMAA_cCyA-3eTFursKAuSQE",
        "CAACAgIAAxkBAAMFYpPW8ryyPLOuQNSqS2n68-vaVaAAAmsAA1m7_CXlhR-IvnQz1CQE",
        "CAACAgQAAxkBAAMdYpPXGNr1AbLvBZGO9lXnyi41sWkAAhwBAALIa1YHtoppM8ds7pEkBA",
        "CAACAgIAAxkBAAMbYpPXFA0M-E9s1QNdSj3gHiX50ToAAqgDAALO2OgLvCw5zTYBCR4kBA",
        "CAACAgIAAxkBAAMZYpPXEv1Dd-tbO9VtS8-9l7oV4K8AAiIKAALvMwABSAU4XZOBmvs4JAQ",
        "AACAgIAAxkBAAMXYpPXEC3OILMoz5X7j8wxX59whhAAAl4BAAIiN44EYhjNgx6ds6skBA",
        "CAACAgIAAxkBAAMVYpPXDqNfX6yWA2z0LE0EysU29V8AAmQSAALo1uISiXEWo2Ui_vEkBA",
        "CAACAgIAAxkBAAMTYpPXCyaXoMLdBUs9J6CQqLQZ200AAooAAxZCawpPg6lZLnBT_iQE",
        "CAACAgIAAxkBAAMRYpPXCCfkPcGF3TCJJFxjq7afUE0AAn8JAAIYQu4I6jCeSfZVCU4kBA",
        "CAACAgIAAxkBAAMPYpPXBsoUmj0DhgAB9qsG_4yTK13fAAKpAAM7YCQUsDXZNYgSZV4kBA",
        "CAACAgIAAxkBAAMNYpPXAZisHfSiaVPhWE6pN_SlmysAAi8AAw220hnw5TZTudzd2SQE",
        "CAACAgIAAxkBAAMLYpPW_vEXNIf3MlerY8Js3JyWxWUAAhoAA61lvBSKvqnX5TcFmyQE",
        "CAACAgEAAxkBAAMJYpPW-2B8furRJMB-uSp1M4nK63sAAvABAAI4DoIRWmGJKXFjdAwkBA",
        "CAACAgIAAxkBAAMHYpPW-MB9RE2t3m5mwrPqbjlLut4AAlIMAAJYoDlKIuw4S1U7DbIkBA",
        "CAACAgIAAxkBAAMFYpPW8ryyPLOuQNSqS2n68-vaVaAAAmsAA1m7_CXlhR-IvnQz1CQE",
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
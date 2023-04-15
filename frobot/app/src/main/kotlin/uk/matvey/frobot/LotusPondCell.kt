package uk.matvey.frobot

sealed class LotusPondCell(
    val symbol: Char,
    val emoji: String,
) {

    object Frobot : LotusPondCell('b', "🐸")

    object Lotus : LotusPondCell('l', "🪷")

    object Fire : LotusPondCell('f', "🔥")

    companion object {

        fun fromSymbol(symbol: Char): LotusPondCell {
            return when (symbol) {
                'b' -> Frobot
                'l' -> Lotus
                'f' -> Fire
                else -> throw IllegalArgumentException()
            }
        }
    }
}

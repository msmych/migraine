package uk.matvey.frobot

sealed class RockGardenCell(
    val symbol: Char,
    val emoji: String,
) {

    object Frog : RockGardenCell('b', "🐸")

    object Rock : RockGardenCell('r', "🪨")

    object Fire : RockGardenCell('f', "🔥")

    object TreasureMap : RockGardenCell('m', "🗺️")

    companion object {

        fun fromSymbol(symbol: Char): RockGardenCell {
            return when (symbol) {
                'b' -> Frog
                'r' -> Rock
                'f' -> Fire
                'm' -> TreasureMap
                else -> throw IllegalArgumentException()
            }
        }
    }
}

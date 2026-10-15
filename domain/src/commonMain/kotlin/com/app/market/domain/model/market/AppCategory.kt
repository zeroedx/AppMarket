package com.app.market.domain.model.market

/** Top-level catalogue sections exposed in the main navigation. */
enum class AppCategory { GAMES, APPS }

/** 小米官方应用细分类（`app.mi.com/categotyAllListApi` 的 categoryId）。 */
enum class AppSubCategory(val xiaomiCategoryId: Int) {
    TOOLS(5),
    MEDIA(27),
    SOCIAL(2),
    OFFICE(10),
    EDUCATION(12),
    SHOPPING(9),
    SPORTS(8),
}

const val XIAOMI_GAME_CATEGORY_ID = 15

/** TapTap 官方游戏榜单类型（`/app-top/v2/hits` 的 `type_name`）。 */
enum class GameRanking(val tapTapType: String) {
    HOT("hot"),
    NEW("new"),
    SELL("sell"),
}

/** 小米官方游戏细分类（同为 `categotyAllListApi` 的 categoryId）。 */
enum class GameSubCategory(val xiaomiCategoryId: Int) {
    ALL(XIAOMI_GAME_CATEGORY_ID),
    STRATEGY(16),
    ACTION(17),
    RACING(18),
    RPG(19),
    CARD(20),
    FIGHTING(21),
    KIDS(22),
    CASUAL(23),
    FLIGHT(25),
    RUNNER(26),
}

/** 来源动态提供的大类（目前仅 OPPO 的分类页）。 */
data class CategoryOption(val id: String, val name: String)

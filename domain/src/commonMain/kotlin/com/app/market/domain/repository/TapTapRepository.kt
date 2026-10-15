package com.app.market.domain.repository

import com.app.market.domain.model.download.DownloadMeta
import com.app.market.domain.model.market.AppDetail
import com.app.market.domain.model.market.GameRanking
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.market.SearchPage
import com.app.market.domain.model.today.TodayArticle
import com.app.market.domain.model.today.TodayFeedPage
import com.app.market.domain.model.update.ManualUpdateRequest
import com.app.market.domain.model.update.ManualUpdateResult

/** TapTap anonymous source used for search, application details, downloads and updates. */
interface TapTapRepository {
    suspend fun search(keyword: String, page: Int = 0): SearchPage
    suspend fun appDetail(appId: Long, packageName: String): AppDetail
    suspend fun todayFeed(page: Int = 0, pageSize: Int = 9): TodayFeedPage
    suspend fun todayArticle(rId: String): TodayArticle

    /** TapTap 官方游戏榜单（热门 / 新品 / 热卖）。 */
    suspend fun rankedGames(ranking: GameRanking, page: Int = 0, pageSize: Int = 20): SearchPage

    suspend fun downloadMeta(app: MarketAppInfo): DownloadMeta
    suspend fun downloadUpdateMeta(app: MarketAppInfo): DownloadMeta
    suspend fun checkUpdates(): List<MarketAppInfo>
    suspend fun checkManualUpdate(request: ManualUpdateRequest): ManualUpdateResult
}

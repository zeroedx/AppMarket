package com.app.market.domain.repository

import com.app.market.domain.model.download.DownloadMeta
import com.app.market.domain.model.market.AppDetail
import com.app.market.domain.model.market.CategoryOption
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.market.SearchPage
import com.app.market.domain.model.today.TodayArticle
import com.app.market.domain.model.today.TodayFeedPage
import com.app.market.domain.model.update.ManualUpdateRequest
import com.app.market.domain.model.update.ManualUpdateResult

/** OPPO/HeyTap store operations. Kept separate so the common source gateway can provide fallbacks. */
interface OppoRepository {
    suspend fun search(keyword: String, page: Int = 0): SearchPage

    /** OPPO 分类页动态提供的大类（应用 / 游戏）。 */
    suspend fun categoryOptions(games: Boolean): List<CategoryOption>

    /** 指定大类的应用列表；[optionId] 为 [categoryOptions] 返回的 id。 */
    suspend fun categoryApps(games: Boolean, optionId: String, page: Int = 0): SearchPage

    suspend fun appDetail(appId: Long, packageName: String, externalQuery: String? = null): AppDetail

    suspend fun downloadMeta(app: MarketAppInfo): DownloadMeta

    suspend fun downloadUpdateMeta(app: MarketAppInfo): DownloadMeta

    suspend fun checkUpdates(): List<MarketAppInfo>

    suspend fun checkManualUpdate(request: ManualUpdateRequest): ManualUpdateResult

    suspend fun beautyFeed(page: Int = 0, pageSize: Int = 10): TodayFeedPage

    suspend fun beautyArticle(snippetId: String): TodayArticle
}

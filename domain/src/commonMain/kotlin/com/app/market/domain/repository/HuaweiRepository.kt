package com.app.market.domain.repository

import com.app.market.domain.model.download.DownloadMeta
import com.app.market.domain.model.market.AppDetail
import com.app.market.domain.model.market.CategoryOption
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.market.SearchPage
import com.app.market.domain.model.update.ManualUpdateRequest
import com.app.market.domain.model.update.ManualUpdateResult

/** Huawei AppGallery anonymous source: search, detail, package delivery, updates and recommendations. */
interface HuaweiRepository {
    suspend fun search(keyword: String, page: Int = 0): SearchPage

    /** 首页应用榜 / 游戏榜（每页 25 条，可连续翻页）。 */
    suspend fun rankedApps(games: Boolean, page: Int = 0): SearchPage

    suspend fun appDetail(appId: Long, packageName: String): AppDetail
    suspend fun downloadMeta(app: MarketAppInfo): DownloadMeta
    suspend fun downloadUpdateMeta(app: MarketAppInfo): DownloadMeta
    suspend fun checkUpdates(): List<MarketAppInfo>
    suspend fun checkManualUpdate(request: ManualUpdateRequest): ManualUpdateResult
}

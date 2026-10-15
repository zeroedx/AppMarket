package com.app.market.data.repository

import com.app.market.domain.model.market.AppSource
import com.app.market.domain.model.market.MarketLink
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Headers
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

/**
 * 用假引擎隔离网络，验证短链接展开：只跟随白名单短链域名，展开结果再交给解析器，
 * 展开失败或指向非商店地址时返回 null。
 */
class MarketLinkResolverTest {

    @Test
    fun resolvesDirectLinkWithoutNetwork() = runBlocking {
        val engine = FakeEngine(emptyMap())
        val resolver = MarketLinkResolverImpl(HttpClient(engine))

        assertEquals(
            MarketLink("com.tencent.mm", AppSource.XIAOMI),
            resolver.resolve("https://app.mi.com/details?id=com.tencent.mm"),
        )
        // 直接命中解析时不应发起任何请求
        assertEquals(emptyList(), engine.requested)
        engine.close()
    }

    @Test
    fun expandsHuaweiShortLinkAndParsesFinalUrl() = runBlocking {
        val engine = FakeEngine(
            mapOf(
                "https://url.cloud.huawei.com/Cn9gXTtP6o" to Route.Redirect(
                    "https://appgallery.huawei.com/app/C100404489?locale=zh_TW",
                ),
            ),
        )
        val resolver = MarketLinkResolverImpl(HttpClient(engine))

        val link = resolver.resolve("华为分享 https://url.cloud.huawei.com/Cn9gXTtP6o")

        assertEquals(MarketLink("", AppSource.HUAWEI, 100404489L), link)
        // 先请求短链接，再跟随 302 请求最终详情页
        assertEquals("https://url.cloud.huawei.com/Cn9gXTtP6o", engine.requested.first())
        assertEquals(
            "https://appgallery.huawei.com/app/C100404489?locale=zh_TW",
            engine.requested.last(),
        )
        engine.close()
    }

    @Test
    fun doesNotFollowNonWhitelistedShortLink() = runBlocking {
        val engine = FakeEngine(emptyMap())
        val resolver = MarketLinkResolverImpl(HttpClient(engine))

        assertNull(resolver.resolve("看看这个 https://www.baidu.com/abc"))
        assertEquals(emptyList(), engine.requested)
        engine.close()
    }

    @Test
    fun returnsNullWhenShortLinkExpansionFails() = runBlocking {
        val engine = FakeEngine(
            mapOf("https://url.cloud.huawei.com/Cn9gXTtP6o" to Route.Failure),
        )
        val resolver = MarketLinkResolverImpl(HttpClient(engine))

        assertNull(resolver.resolve("https://url.cloud.huawei.com/Cn9gXTtP6o"))
        assertEquals(listOf("https://url.cloud.huawei.com/Cn9gXTtP6o"), engine.requested)
        engine.close()
    }

    @Test
    fun returnsNullWhenExpandedUrlIsNotAStoreLink() = runBlocking {
        val engine = FakeEngine(
            mapOf("https://url.cloud.huawei.com/Cn9gXTtP6o" to Route.Redirect("https://example.com/landing")),
        )
        val resolver = MarketLinkResolverImpl(HttpClient(engine))

        assertNull(resolver.resolve("https://url.cloud.huawei.com/Cn9gXTtP6o"))
        engine.close()
    }
}

private sealed interface Route {
    data class Redirect(val location: String) : Route
    data object Ok : Route
    data object Failure : Route
}

/** 只认识预置路由的假引擎，记录被请求的 URL，用于断言是否真的发起了短链展开。 */
@OptIn(InternalAPI::class)
private class FakeEngine(private val routes: Map<String, Route>) : HttpClientEngineBase("fake") {
    val requested = mutableListOf<String>()
    override val config: HttpClientEngineConfig = HttpClientEngineConfig()

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val url = data.url.toString()
        requested += url
        return when (val route = routes[url] ?: Route.Ok) {
            Route.Ok -> response(HttpStatusCode.OK, callContext = callContext)
            Route.Failure -> error("unexpected request to $url")
            is Route.Redirect ->
                response(HttpStatusCode.Found, mapOf(HttpHeaders.Location to route.location), callContext)
        }
    }

    private fun response(
        status: HttpStatusCode,
        extraHeaders: Map<String, String> = emptyMap(),
        callContext: CoroutineContext,
    ) = HttpResponseData(
        statusCode = status,
        requestTime = GMTDate(),
        headers = Headers.build { extraHeaders.forEach { (k, v) -> append(k, v) } },
        version = HttpProtocolVersion.HTTP_1_1,
        body = ByteReadChannel.Empty,
        callContext = callContext,
    )
}

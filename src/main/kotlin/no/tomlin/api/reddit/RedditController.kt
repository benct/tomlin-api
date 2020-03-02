package no.tomlin.api.reddit

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.common.JsonUtils.parseJson
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reddit")
class RedditController(private val fetcher: HttpFetcher = fetcher("https://www.reddit.com")) {

    @Secured(USER, ADMIN)
    @GetMapping("/r/{sub}", "/r/{sub}/{after}")
    fun sub(@PathVariable sub: String, @PathVariable after: String?): Map<String, Any?> {
        val response = fetcher.getJson("/r/${sub}.json", mapOf("raw_json" to "1").plus("after" to after))
            .readBody()
            .parseJson<RedditReponse>(SNAKE_CASE)

        return mapOf(
            "sub" to sub,
            "after" to response.data.after,
            "before" to response.data.before,
            "posts" to response.data.children
                .map { it.data.crosspostParentList?.firstOrNull() ?: it.data }
                .filterNot { it.isSelf }
                .map {
                    mapOf(
                        "name" to it.name,
                        "author" to it.author,
                        "title" to it.title,
                        "url" to it.url,
                        "link" to "https://www.reddit.com${it.permalink}",
                        "timestamp" to it.createdUTC * 1000,
                        "ups" to it.ups,
                        "downs" to it.downs,
                        "type" to (it.postHint ?: "external"),
                        "isRedditDomain" to it.isRedditMediaDomain,
                        "image" to it.preview?.images?.firstOrNull()?.source?.url,
                        "video" to (it.preview?.redditVideoPreview ?: it.secureMedia?.redditVideo)
                    )
                }
        )
    }

    data class RedditReponse(
        val kind: String,
        val data: RedditData
    )

    data class RedditData(
        val children: List<RedditItem>,
        val after: String,
        val before: String? = null
    )

    data class RedditItem(
        val kind: String,
        val data: RedditPost
    )

    data class RedditPost(
        val subreddit: String,
        val id: String,
        val subredditID: String,
        val subredditType: String,
        val subredditNamePrefixed: String,
        val subredditSubscribers: Long,
        val name: String,
        val title: String,
        val author: String,
        val authorFullname: String? = null,
        val authorPremium: Boolean,
        val ups: Long,
        val downs: Long,
        val score: Long,
        val numComments: Long,
        val postHint: String? = null,
        val url: String,
        val permalink: String,
        val created: Long,
        val createdUTC: Long,
        val thumbnail: String,
        val thumbnailWidth: Long? = null,
        val thumbnailHeight: Long? = null,
        val preview: RedditPreview? = null,
        val media: RedditMedia? = null,
        val secureMedia: RedditMedia? = null,
        val mediaEmbed: RedditMediaEmbed,
        val secureMediaEmbed: RedditMediaEmbed,
        val isSelf: Boolean,
        val isOriginalContent: Boolean,
        val isRedditMediaDomain: Boolean,
        val isVideo: Boolean,
        val isMeta: Boolean,
        val quarantine: Boolean,
        val hidden: Boolean,
        val stickied: Boolean,
        val pinned: Boolean,
        val spoiler: Boolean,
        val locked: Boolean,
        val archived: Boolean,
        val mediaOnly: Boolean,
        val over18: Boolean,
        val category: Any? = null,
        val contentCategories: Any? = null,
        val domain: String,
        val selftext: String,
        val selftextHTML: String? = null,
        val crosspostParentList: List<RedditPost>? = null
    )

    data class RedditMedia(
        val type: String? = null,
        val redditVideo: RedditVideo? = null
    )

    data class RedditVideo(
        val fallbackURL: String,
        val hlsURL: String,
        val dashURL: String,
        val scrubberMediaURL: String,
        val height: Long,
        val width: Long,
        val duration: Long,
        val isGIF: Boolean
    )

    data class RedditMediaEmbed(
        val content: String? = null,
        val width: Long? = null,
        val height: Long? = null,
        val scrolling: Boolean? = null,
        val mediaDomainURL: String? = null
    )

    data class RedditPreview(
        val images: List<RedditImage>? = null,
        val enabled: Boolean? = null,
        val redditVideoPreview: RedditVideo? = null
    )

    data class RedditImage(
        val source: RedditIcon? = null,
        val resolutions: List<RedditIcon>? = null,
        val id: String? = null
    )

    data class RedditIcon(
        val url: String,
        val width: Long,
        val height: Long
    )
}
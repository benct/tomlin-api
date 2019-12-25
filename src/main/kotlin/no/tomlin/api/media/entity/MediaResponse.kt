package no.tomlin.api.media.entity

import no.tomlin.api.common.Constants
import kotlin.math.ceil

data class MediaResponse(
    val page: Int,
    val total_pages: Int,
    val total_results: Int,
    val results: List<Map<String, Any?>>
) {
    constructor(page: Int, total: Int, data: List<Map<String, Any?>>) : this(
        page,
        ceil(total.toDouble() / Constants.PAGE_SIZE).toInt(),
        total,
        data
    )
}
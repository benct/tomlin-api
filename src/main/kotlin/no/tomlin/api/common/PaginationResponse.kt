package no.tomlin.api.common

import kotlin.math.ceil

data class PaginationResponse<T>(
    val page: Int,
    val total_pages: Int,
    val total_results: Int,
    val results: List<T>
) {
    constructor(data: List<T>) : this(1, data.size, data)

    constructor(page: Int, total: Int, data: List<T>) : this(
        page,
        ceil(total.toDouble() / Constants.PAGE_SIZE).toInt(),
        total,
        data
    )
}

package no.tomlin.api.common

data class Sort(val columns: List<String>, val direction: String) {
    constructor(columns: String, direction: String) : this(columns.split(",").map { it.trim() }, direction)

    fun toPairs(): Array<Pair<String, String>> = columns.map { it to direction }.toTypedArray()
}

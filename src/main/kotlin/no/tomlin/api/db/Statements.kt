package no.tomlin.api.db

data class Select private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(from: Table) : this("SELECT * FROM $from", emptyMap())
    constructor(
        columns: String = "*",
        from: Table,
        join: Join? = null,
        where: Where? = null,
        groupBy: GroupBy? = null,
        orderBy: OrderBy? = null,
        limit: Int? = null,
        offset: Int? = null,
    ) : this(
        listOfNotNull(
            "SELECT $columns",
            "FROM $from AS ${from.alias}",
            join?.statement,
            where?.statement,
            groupBy?.statement,
            orderBy?.statement,
            limit?.let { "LIMIT $it" },
            offset?.let { "OFFSET $it" },
        ).joinToString(" "),
        where?.data ?: emptyMap()
    )
}

data class Insert private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(into: Table, data: Map<String, Any?>) : this(
        "INSERT INTO $into (${data.keys.joinToString { "`$it`" }}) VALUES (${data.keys.joinToString { ":$it" }})",
        data
    )
}

data class Upsert private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(into: Table, data: Map<String, Any?>, incrementOnDuplicate: String? = null) : this(
        "INSERT INTO $into (${data.keys.joinToString { "`$it`" }}) VALUES (${data.keys.joinToString { ":$it" }}) " +
            "ON DUPLICATE KEY UPDATE ${data.keys.joinToString { "`$it` = :$it" }}" +
            incrementOnDuplicate?.let { ", `$it` = `$it` + 1" }.orEmpty(),
        data
    )
}

data class Update private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(table: Table, set: Map<String, Any?>, where: Where) :
        this("UPDATE $table SET ${set.keys.joinToString { "`$it` = :$it" }} ${where.statement}", set.plus(where.data))
}

data class Delete private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(table: Table) : this("DELETE FROM $table", emptyMap())
    constructor(table: Table, where: Where) : this("DELETE FROM $table ${where.statement}", where.data)
}

data class Increment private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(table: Table, column: String, where: Where) :
        this("UPDATE $table SET `$column` = `$column` + 1 ${where.statement}", where.data)
}

data class Decrement private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(table: Table, column: String, where: Where) :
        this("UPDATE $table SET `$column` = `$column` - 1 ${where.statement}", where.data)
}

data class Where private constructor(val statement: String, val data: Map<String, Any?>) {
    constructor(vararg data: Pair<String, Any?>, separator: String = "AND", like: Boolean = false) :
        this(
            "WHERE ${data.joinToString(" $separator ") { "${it.first} ${if (like) "LIKE" else "="} :${it.first}" }}",
            data.toMap()
        )
}

data class GroupBy private constructor(val statement: String) {
    constructor(vararg columns: String) : this("GROUP BY ${columns.joinToString()}")
}

data class OrderBy private constructor(val statement: String) {
    constructor(column: String, direction: String = "ASC") : this("ORDER BY $column $direction")
    constructor(vararg columns: List<String>) : this("ORDER BY ${columns.joinToString { "$it ASC" }}")
    constructor(vararg pairs: Pair<String, String>) : this("ORDER BY ${pairs.joinToString { "${it.first} ${it.second}" }}")
}

data class Join private constructor(val statement: String) {
    constructor(table: Table, vararg on: Pair<String, String>) :
        this("JOIN $table AS ${table.alias} ON ${on.joinToString { "${it.first} = ${it.second}" }}")
}

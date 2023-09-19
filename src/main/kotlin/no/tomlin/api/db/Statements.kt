package no.tomlin.api.db

import no.tomlin.api.common.Extensions.ifNotEmpty
import org.springframework.dao.DataAccessException

class Select(val from: Table) : WhereStatement<Select>(from) {
    private val cols: MutableList<String> = mutableListOf()
    private val joins: MutableList<String> = mutableListOf()
    private val groupBy: MutableList<String> = mutableListOf()
    private val orderBy: MutableList<String> = mutableListOf()
    private var limit: Int? = null
    private var offset: Int = 0

    fun columns(vararg columns: String): Select = columns(from, *columns)

    fun columns(table: Table, vararg columns: String): Select = apply {
        cols.addAll(columns.map { "$table.${if (it == "*") it else "`$it`"}" })
    }

    fun column(select: Select, columnAs: String): Select = apply {
        cols.add("(${select.statement}) AS `$columnAs`")
        namedParameters.putAll(select.data)
    }

    fun column(column: String) = CustomColumn(null, column)

    fun column(table: Table, column: String) = CustomColumn(table, column)

    inner class CustomColumn(private val table: Table?, private val column: String) {
        fun count(columnAs: String? = null): Select {
            cols.add(build("COUNT(?)", columnAs))
            return this@Select
        }

        fun countDistinct(columnAs: String? = null): Select {
            cols.add(build("COUNT(DISTINCT ?)", columnAs))
            return this@Select
        }

        fun distinct(columnAs: String? = null): Select {
            cols.add(build("DISTINCT ?", columnAs))
            return this@Select
        }

        fun avg(columnAs: String? = null): Select {
            cols.add(build("AVG(?)", columnAs))
            return this@Select
        }

        fun sum(columnAs: String? = null): Select {
            cols.add(build("SUM(?)", columnAs))
            return this@Select
        }

        fun floor(columnAs: String? = null): Select {
            cols.add(build("FLOOR(?)", columnAs))
            return this@Select
        }

        fun groupConcat(columnAs: String? = null): Select {
            cols.add(build("GROUP_CONCAT(?)", columnAs))
            return this@Select
        }

        fun subString(start: Int, end: Int, columnAs: String? = null): Select {
            cols.add(build("SUBSTRING(?, $start, $end)", columnAs))
            return this@Select
        }

        fun custom(columnAs: String? = null): Select {
            cols.add(column + columnAs?.let { " AS `$it`" }.orEmpty())
            return this@Select
        }

        private fun build(template: String, columnAs: String?): String =
            template.replace("?", "${table ?: from}.`$column`") + columnAs?.let { " AS `$it`" }.orEmpty()
    }

    fun join(table: Table) = JoinCondition(table)

    inner class JoinCondition(private val table: Table) {
        fun on(joinTableColumn: String, baseTableColumn: String): Select {
            joins.add("JOIN $table ON $table.`${joinTableColumn}` = ${from}.`${baseTableColumn}`")
            return this@Select
        }

        fun on(joinTableColumn: String, otherTableColumn: String, otherTable: Table): Select {
            joins.add("JOIN $table ON $table.`${joinTableColumn}` = ${otherTable}.`${otherTableColumn}`")
            return this@Select
        }
    }

    fun groupBy(vararg columns: String): Select = groupBy(from, *columns)

    fun groupBy(table: Table, vararg columns: String): Select = apply {
        groupBy.addAll(columns.map { "$table.`$it`" })
    }

    fun groupByAlias(vararg columnAlias: String): Select = apply {
        groupBy.addAll(columnAlias.map { "`$it`" })
    }

    fun orderBy(vararg columns: String): Select = apply {
        orderBy.addAll(columns.map { "$from.`$it`" })
    }

    fun orderBy(vararg columns: Pair<String, String>): Select = orderBy(from, *columns)

    fun orderBy(table: Table, vararg columns: Pair<String, String>): Select = apply {
        orderBy.addAll(columns.map { "$table.`${it.first}` ${it.second}" })
    }

    fun limit(size: Int? = null, offset: Int = 0): Select = apply {
        this.limit = size
        this.offset = offset
    }

    override fun build(): String =
        "SELECT ${cols.joinToString(", ").ifEmpty { "$from.*" }} FROM $from" +
            joins.joinToString(" ").ifNotEmpty { " $it" } +
            where.joinToString(" ").ifNotEmpty { " WHERE $it" } +
            groupBy.joinToString(", ").ifNotEmpty { " GROUP BY $it" } +
            orderBy.joinToString(", ").ifNotEmpty { " ORDER BY $it" } +
            limit?.let { " LIMIT $it OFFSET $offset" }.orEmpty()

    override fun getThis(): Select = this
}

class Insert(into: Table) : InsertStatement<Insert>(into) {
    override fun getThis(): Insert = this
}

class Upsert(into: Table) : InsertStatement<Upsert>(into) {
    private var incrementColumn: String? = null

    fun incrementOnUpdate(column: String): Upsert = apply {
        incrementColumn = column
    }

    override fun build(): String =
        "${super.build()} ON DUPLICATE KEY UPDATE ${namedParameters.keys.joinToString { "`$it` = :$it" }}" +
            incrementColumn?.let { ", `$it` = `$it` + 1" }.orEmpty()

    override fun getThis(): Upsert = this
}

class Update(private val table: Table) : WhereStatement<Update>(table) {
    private val setKeyVal: MutableList<String> = mutableListOf()

    fun set(vararg data: Pair<String, Any?>): Update = apply {
        setKeyVal.addAll(data.map { "`${it.first}` = :${it.first}" })
        namedParameters.putAll(data)
    }

    fun setRaw(vararg data: Pair<String, String>): Update = apply {
        setKeyVal.addAll(data.map { "`${it.first}` = ${it.second}" })
    }

    override fun build(): String =
        "UPDATE $table SET ${setKeyVal.joinToString(", ")} WHERE ${where.joinToString(" ")}"

    override fun getThis(): Update = this
}

class Delete(private val from: Table) : WhereStatement<Delete>(from) {
    override fun build(): String =
        "DELETE FROM $from" + where.joinToString(" ").ifNotEmpty { " WHERE $it" }

    override fun getThis(): Delete = this
}

class Increment(table: Table) : IncDecStatement(table, '+')
class Decrement(table: Table) : IncDecStatement(table, '-')

abstract class IncDecStatement(private val table: Table, private val plusOrMinus: Char) :
    WhereStatement<IncDecStatement>(table) {

    private var column: String? = null

    fun column(name: String): IncDecStatement = apply {
        column = name
    }

    override fun build(): String =
        "UPDATE $table SET `$column` = `$column` $plusOrMinus 1 WHERE ${where.joinToString(" ")}"

    override fun getThis(): IncDecStatement = this
}

abstract class InsertStatement<T>(private val into: Table) : Statement<T>() {
    fun data(vararg data: Pair<String, Any?>): T {
        namedParameters.putAll(data)
        return getThis()
    }

    fun data(data: Map<String, Any?>): T {
        namedParameters.putAll(data)
        return getThis()
    }

    override fun build(): String =
        "INSERT INTO $into (${namedParameters.keys.joinToString { "`$it`" }}) " +
            "VALUES (${namedParameters.keys.joinToString { ":$it" }})"
}

abstract class WhereStatement<T>(private val from: Table) : Statement<T>() {
    protected val where: MutableList<String> = mutableListOf()

    fun where(column: String) = Condition(from, column)
    fun where(table: Table, column: String) = Condition(table, column)

    fun and(column: String) = Condition(from, column, "AND")
    fun and(table: Table, column: String) = Condition(table, column, "AND")

    fun or(column: String) = Condition(from, column, "OR")
    fun or(table: Table, column: String) = Condition(table, column, "OR")

    inner class Condition(private val table: Table, private val column: String, operator: String? = null) {
        private val op = operator?.let { "$it " }.orEmpty()

        init {
            if (operator != null && where.isEmpty()) {
                throw IllegalSqlStatementOrderException("Cannot call `and` or `or` before `where`.")
            }
        }

        fun eq(otherTable: Table, otherColumn: String): T {
            where.add("$op$table.`$column` = $otherTable.`$otherColumn`")
            return getThis()
        }

        fun eq(value: Any?): T {
            where.add("$op$table.`$column` = :${table}_$column")
            namedParameters["${table}_$column"] = value
            return getThis()
        }

        fun like(value: Any?): T {
            where.add("$op$table.`$column` LIKE :${table}_$column")
            namedParameters["${table}_$column"] = value
            return getThis()
        }
    }
}

abstract class Statement<T> {
    protected val namedParameters: MutableMap<String, Any?> = mutableMapOf()

    val statement: String
        get() = build()

    val data: Map<String, Any?>
        get() = namedParameters.toMap()

    abstract fun build(): String

    abstract fun getThis(): T

    protected class IllegalSqlStatementOrderException(message: String) : DataAccessException(message)
}

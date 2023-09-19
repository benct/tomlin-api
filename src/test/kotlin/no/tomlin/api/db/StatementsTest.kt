package no.tomlin.api.db

import no.tomlin.api.db.Table.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatementsTest {

    @Test
    fun buildsCorrectSelectStatements() {
        assertEquals("SELECT $tbl.* FROM $tbl", Select(tbl).build())
        assertEquals("SELECT $tbl.* FROM $tbl", Select(tbl).columns("*").build())
        assertEquals("SELECT $tbl.`name` FROM $tbl", Select(tbl).columns("name").build())
        assertEquals(
            "SELECT $tbl.`name`, $tbl.`email` FROM $tbl",
            Select(tbl).columns("name", "email").build()
        )
        assertEquals("SELECT $tbl.`name` FROM $tbl", Select(tbl).columns(tbl, "name").build())
        assertEquals(
            "SELECT COUNT($tbl.`id`) AS `total` FROM $tbl",
            Select(tbl).column("id").count("total").build()
        )
        assertEquals(
            "SELECT COUNT($tbl2.`id`) FROM $tbl",
            Select(tbl).column(tbl2, "id").count().build()
        )
        assertEquals(
            "SELECT (SELECT COUNT($tbl2.`id`) FROM $tbl2) AS `total` FROM $tbl",
            Select(tbl)
                .column(Select(tbl2).column("id").count(), "total")
                .build()
        )
    }

    @Test
    fun buildsCorrectSelectStatementsWithJoin() {
        assertEquals(
            "SELECT $tbl.* FROM $tbl JOIN $tbl2 ON $tbl2.`user_id` = $tbl.`id`",
            Select(tbl).join(tbl2).on("user_id", "id").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl JOIN $tbl2 ON $tbl2.`user_id` = $tbl.`id` GROUP BY $tbl.`id`, $tbl.`name`, $tbl2.`user_id`",
            Select(tbl)
                .join(tbl2).on("user_id", "id")
                .groupBy("id", "name")
                .groupBy(tbl2, "user_id")
                .build()
        )
    }

    @Test
    fun buildsCorrectSelectStatementsWithWhere() {
        assertEquals(
            "SELECT $tbl.* FROM $tbl WHERE $tbl.`name` = :${tbl}_name",
            Select(tbl)
                .where("name")
                .eq("test")
                .build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl WHERE $tbl.`name` = :${tbl}_name AND $tbl.`enabled` = :${tbl}_enabled",
            Select(tbl)
                .where("name").eq("test")
                .and("enabled").eq(true)
                .build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl WHERE $tbl.`name` = :${tbl}_name OR $tbl.`enabled` = :${tbl}_enabled",
            Select(tbl)
                .where("name").eq("test")
                .or("enabled").eq(true)
                .build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl WHERE $tbl.`name` LIKE :${tbl}_name",
            Select(tbl)
                .where("name").like("%test%")
                .build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl WHERE $tbl2.`role` = :${tbl2}_role AND $tbl.`role_id` = $tbl2.`id`",
            Select(tbl)
                .where(tbl2, "role").eq("test")
                .and("role_id").eq(tbl2, "id")
                .build()
        )
    }

    @Test
    fun buildsCorrectSelectStatementsWithOrderBy() {
        assertEquals(
            "SELECT $tbl.* FROM $tbl ORDER BY $tbl.`name`",
            Select(tbl).orderBy("name").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl ORDER BY $tbl.`name`, $tbl.`email`",
            Select(tbl).orderBy("name", "email").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl ORDER BY $tbl.`name` DESC",
            Select(tbl).orderBy("name" to "DESC").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl ORDER BY $tbl.`name` DESC, $tbl.`email` ASC",
            Select(tbl).orderBy("name" to "DESC", "email" to "ASC").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl ORDER BY $tbl2.`name` DESC",
            Select(tbl).orderBy(tbl2, "name" to "DESC").build()
        )
    }

    @Test
    fun buildsCorrectSelectStatementsWithGroupBy() {
        assertEquals(
            "SELECT $tbl.* FROM $tbl GROUP BY $tbl.`name`",
            Select(tbl).groupBy("name").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl GROUP BY $tbl.`name`, $tbl.`email`",
            Select(tbl).groupBy("name", "email").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl GROUP BY $tbl2.`name`",
            Select(tbl).groupBy(tbl2, "name").build()
        )
        assertEquals(
            "SELECT $tbl.* FROM $tbl GROUP BY `customColumn`",
            Select(tbl).groupByAlias("customColumn").build()
        )
    }

    @Test
    fun buildsCorrectComplexSelectStatementAndData() {
        val select = Select(tbl)
            .columns("name", "email", "phone")
            .columns(tbl2, "role")
            .column("id").count("total")
            .join(tbl2).on("email", "email")
            .where("email").eq("test")
            .or("name").like("%something%")
            .groupBy("email", "name")
            .orderBy("name" to "DESC")
            .limit(10, offset = 5)

        assertEquals(
            "SELECT $tbl.`name`, $tbl.`email`, $tbl.`phone`, $tbl2.`role`, COUNT($tbl.`id`) AS `total` " +
                "FROM $tbl " +
                "JOIN $tbl2 ON $tbl2.`email` = $tbl.`email` " +
                "WHERE $tbl.`email` = :${tbl}_email OR $tbl.`name` LIKE :${tbl}_name " +
                "GROUP BY $tbl.`email`, $tbl.`name` " +
                "ORDER BY $tbl.`name` DESC " +
                "LIMIT 10 OFFSET 5",
            select.statement
        )
        assertEquals(
            mapOf("${tbl}_email" to "test", "${tbl}_name" to "%something%"),
            select.data
        )
    }

    @Test
    fun buildsCorrectSelectStatementWithSubQuery() {
        val select = Select(TABLE_TV)
            .columns("*")
            .column("'tv'").custom("type")
            .column(
                Select(TABLE_EPISODE)
                    .column("id").count()
                    .where("seen").eq(true)
                    .and("tv_id").eq(TABLE_TV, "id"),
                "seen_episodes"
            )
            .where("seen").eq(false)
            .orderBy("release_date")

        assertEquals(
            "SELECT $tv.*, 'tv' AS `type`, " +
                "(SELECT COUNT($episode.`id`) FROM $episode " +
                "WHERE $episode.`seen` = :${episode}_seen " +
                "AND $episode.`tv_id` = $tv.`id`) AS `seen_episodes` " +
                "FROM $tv " +
                "WHERE $tv.`seen` = :${tv}_seen " +
                "ORDER BY $tv.`release_date`",
            select.statement
        )
        assertEquals(
            mapOf("${episode}_seen" to true, "${tv}_seen" to false),
            select.data
        )
    }

    @Test
    fun buildsCorrectInsertStatements() {
        assertEquals("INSERT INTO $tbl (`name`) VALUES (:name)", Insert(tbl).data("name" to "test").build())

        val insert = Insert(tbl).data("name" to "test", "email" to "some@mail")
        assertEquals("INSERT INTO $tbl (`name`, `email`) VALUES (:name, :email)", insert.statement)
        assertEquals(mapOf("name" to "test", "email" to "some@mail"), insert.data)
    }

    @Test
    fun buildsCorrectUpsertStatements() {
        assertEquals(
            "INSERT INTO $tbl (`name`) VALUES (:name) ON DUPLICATE KEY UPDATE `name` = :name",
            Upsert(tbl).data("name" to "test").build()
        )
        assertEquals(
            "INSERT INTO $tbl (`name`) VALUES (:name) " +
                "ON DUPLICATE KEY UPDATE `name` = :name, `visits` = `visits` + 1",
            Upsert(tbl).data("name" to "test").incrementOnUpdate("visits").build()
        )

        val upsert = Upsert(tbl).data("name" to "test", "email" to "some@mail")
        assertEquals(
            "INSERT INTO $tbl (`name`, `email`) VALUES (:name, :email) " +
                "ON DUPLICATE KEY UPDATE `name` = :name, `email` = :email",
            upsert.statement
        )
        assertEquals(mapOf("name" to "test", "email" to "some@mail"), upsert.data)
    }

    @Test
    fun buildsCorrectUpdateStatements() {
        assertEquals(
            "UPDATE $tbl SET `name` = :name WHERE $tbl.`email` = :${tbl}_email",
            Update(tbl).set("name" to "test").where("email").eq("email").build()
        )
        assertEquals(
            "UPDATE $tbl SET `updated` = CURRENT_TIMESTAMP() WHERE $tbl.`email` = :${tbl}_email",
            Update(tbl).setRaw("updated" to "CURRENT_TIMESTAMP()").where("email").eq("email").build()
        )

        val insert = Update(tbl).set("name" to "test").where("email").eq("some@mail")
        assertEquals("UPDATE $tbl SET `name` = :name WHERE $tbl.`email` = :${tbl}_email", insert.statement)
        assertEquals(mapOf("name" to "test", "${tbl}_email" to "some@mail"), insert.data)
    }

    @Test
    fun buildsCorrectDeleteStatements() {
        assertEquals("DELETE FROM $tbl", Delete(tbl).build())
        assertEquals("DELETE FROM $tbl WHERE $tbl.`name` = :${tbl}_name", Delete(tbl).where("name").eq("test").build())

        val delete = Delete(tbl).where("name").eq("test").and("email").eq("some@mail")
        assertEquals(
            "DELETE FROM $tbl WHERE $tbl.`name` = :${tbl}_name AND $tbl.`email` = :${tbl}_email",
            delete.statement
        )
        assertEquals(mapOf("${tbl}_name" to "test", "${tbl}_email" to "some@mail"), delete.data)
    }

    @Test
    fun buildsCorrectIncrementDecrementStatements() {
        assertEquals(
            "UPDATE $tbl SET `visits` = `visits` + 1 WHERE $tbl.`name` = :${tbl}_name",
            Increment(tbl).column("visits").where("name").eq("test").build()
        )
        assertEquals(
            "UPDATE $tbl SET `visits` = `visits` - 1 WHERE $tbl.`name` = :${tbl}_name",
            Decrement(tbl).column("visits").where("name").eq("test").build()
        )
    }

    companion object {
        val tbl: Table = TABLE_USER
        val tbl2: Table = TABLE_ROLE
        val tv: Table = TABLE_TV
        val episode: Table = TABLE_EPISODE
    }
}
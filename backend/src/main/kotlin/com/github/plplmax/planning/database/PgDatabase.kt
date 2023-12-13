package com.github.plplmax.planning.database

import com.github.plplmax.planning.database.tables.Tables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import io.ktor.util.logging.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class PgDatabase(
    private val config: ApplicationConfig,
    private val development: Boolean,
    private val logger: Logger = LoggerFactory.getLogger("PgDatabase")
) : AppDatabase {
    private val dataSource: DataSource by lazy {
        HikariDataSource(
            HikariConfig().apply {
                poolName = "PgPool"
                jdbcUrl = config.property("jdbcUrl").getString()
                driverClassName = config.property("driverClassName").getString()
                username = config.property("username").getString()
                password = config.property("password").getString()
                maximumPoolSize = 2
                isAutoCommit = false
            }
        )
    }

    private val database: Database by lazy { Database.connect(dataSource) }

    override fun connected(): Database = database

    override fun create(tables: Tables) {
        Flyway.configure().dataSource(dataSource).load().migrate()
        if (!development) return
        transaction(database) {
            val statements = SchemaUtils.statementsRequiredToActualizeScheme(*tables.all().toTypedArray())
            if (statements.isEmpty()) {
                logger.info("Database's schema is up-to-date")
            } else {
                logger.warn("Database's schema should be updated: $statements")
            }
        }
    }
}
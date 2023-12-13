package com.github.plplmax.planning.database

import com.github.plplmax.planning.database.tables.Tables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class H2Database(private val config: ApplicationConfig) : AppDatabase {
    private val dataSource: DataSource by lazy {
        HikariDataSource(
            HikariConfig().apply {
                poolName = "H2Pool"
                jdbcUrl = config.property("jdbcUrl").getString()
                driverClassName = config.property("driverClassName").getString()
                maximumPoolSize = 2
                isAutoCommit = false
            }
        )
    }

    private val database: Database by lazy { Database.connect(dataSource) }

    override fun connected(): Database = database

    override fun create(tables: Tables) {
        transaction(database) {
            // @todo SchemaUtils.create(tables...)
        }
    }
}

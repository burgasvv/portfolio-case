package org.burgas.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseConnection {

    private val config = ApplicationConfig("application.yaml")

    private val hikariConfig = HikariConfig()

    init {
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = config.property("postgres.url").getString()
        hikariConfig.username = config.property("postgres.user").getString()
        hikariConfig.password = config.property("postgres.password").getString()
        hikariConfig.isAutoCommit = false
        hikariConfig.schema = "public"
        hikariConfig.minimumIdle = 5
        hikariConfig.maximumPoolSize = 100
    }

    val postgres = Database.connect(
        datasource = HikariDataSource(hikariConfig),
        databaseConfig = DatabaseConfig { explicitDialect = PostgreSQLDialect() }
    )
}
package org.burgas.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.burgas.dao.IdentityEntity
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.regexp
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.UUID

object DatabaseConnection {

    private val config = ApplicationConfig("application.yaml")

    private val hikariConfig = HikariConfig()

    init {
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = config.property("postgres.url").getString()
        hikariConfig.username = config.property("postgres.user").getString()
        hikariConfig.password = config.property("postgres.password").getString()
        hikariConfig.schema = "public"
        hikariConfig.minimumIdle = 5
        hikariConfig.maximumPoolSize = 100
    }

    val postgres = Database.connect(
        datasource = HikariDataSource(hikariConfig),
        databaseConfig = DatabaseConfig { explicitDialect = PostgreSQLDialect() }
    )
}

object ImageTable : UUIDTable("image") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 100).check { it like "image/%" }
    val preview = bool("preview").default(true)
    val data = blob("data")
}

object VideoTable : UUIDTable("video") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 100).check { it like "video/%" }
    val data = blob("data")
}

object DocumentTable : UUIDTable("document") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 100).check { it like "application/%" }
    val data = blob("data")
}

enum class Authority {
    ADMIN, USER
}

object IdentityTable : UUIDTable("identity") {
    val authority = enumerationByName<Authority>("authority", 100).default(Authority.USER)
    val email = varchar("email", 100).uniqueIndex()
        .check { it regexp "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$" }
    val password = varchar("password", 100)
    val status = bool("status").default(true)
    val phone = varchar("phone", 20).uniqueIndex().check { it regexp "^\\+?[0-9\\s\\-\\(\\)]{7,25}$" }
    val telegram = varchar("telegram", 100).nullable().uniqueIndex().check { it like "@%" }
    val whatsapp = varchar("whatsapp", 100).nullable().uniqueIndex().check { it like "@%" }
    val max = varchar("max", 100).nullable().uniqueIndex().check { it like "@%" }
    val firstname = varchar("firstname", 100)
    val lastname = varchar("lastname", 100)
    val patronymic = varchar("patronymic", 100)
    val about = text("about").nullable()
    val imageId = optReference(
        name = "image_id", refColumn = ImageTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
}

object ProfessionTable : UUIDTable("profession") {
    val name = varchar("name", 250).uniqueIndex()
    val description = text("description").uniqueIndex()
}

object PortfolioTable : UUIDTable("portfolio") {
    val professionId = optReference(
        name = "profession_id", refColumn = ProfessionTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    )
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val description = text("description").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    init {
        uniqueIndex(professionId, identityId)
    }
}

object ProjectTable : UUIDTable("project") {
    val name = varchar("name", 250)
    val description = text("description").nullable()
    val portfolioId = reference(
        name = "portfolio_id", refColumn = PortfolioTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val link = text("link").nullable().uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val imageId = optReference(
        name = "image_id", refColumn = ImageTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
}

object ProjectVideoTable : Table("project_video") {
    val projectId = reference(
        name = "project_id", refColumn = ProjectTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val videoId = reference(
        name = "video_id", refColumn = VideoTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(projectId, videoId))
}

object ProjectDocumentTable : Table("project_document") {
    val projectId = reference(
        name = "project_id", refColumn = ProjectTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val documentId = reference(
        name = "document_Id", refColumn = DocumentTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(projectId, documentId))
}

suspend fun configureDatabase() = suspendTransaction(
    db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
) {
    SchemaUtils.create(
        ImageTable, VideoTable, DocumentTable, IdentityTable, ProfessionTable,
        PortfolioTable, ProjectTable, ProjectVideoTable, ProjectDocumentTable
    )
    val adminId = UUID.fromString("85197047-0962-4848-b46d-8c064879fe0b")
    IdentityEntity.findById(adminId) ?: IdentityEntity.new(adminId) {
        this.authority = Authority.ADMIN
        this.email = "burgasvv@gmail.com"
        this.password = BCrypt.hashpw("burgasvv", BCrypt.gensalt())
        this.phone = "+79563214596"
        this.telegram = "@burgasvv"
        this.whatsapp = "@burgvv"
        this.max = "@burgass"
        this.firstname = "Бургас"
        this.lastname = "Вячеслав"
        this.patronymic = "Васильевич"
        this.about = "Личная информация о Бургас Вячеславе Васильевиче"
    }
}
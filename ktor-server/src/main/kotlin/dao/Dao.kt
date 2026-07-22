package org.burgas.dao

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.burgas.database.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

interface Uploader {
    suspend fun upload(fileItem: PartData.FileItem)
}

class ImageEntity(id: EntityID<UUID>) : UUIDEntity(id), Uploader {
    companion object : UUIDEntityClass<ImageEntity>(ImageTable)

    var name by ImageTable.name
    var contentType by ImageTable.contentType
    var preview by ImageTable.preview
    var data by ImageTable.data

    @OptIn(InternalAPI::class)
    override suspend fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.preview = true
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }
}

class VideoEntity(id: EntityID<UUID>) : UUIDEntity(id), Uploader {
    companion object : UUIDEntityClass<VideoEntity>(VideoTable)

    var name by VideoTable.name
    var contentType by VideoTable.contentType
    var data by VideoTable.data

    @OptIn(InternalAPI::class)
    override suspend fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }
}

class DocumentEntity(id: EntityID<UUID>) : UUIDEntity(id), Uploader {
    companion object : UUIDEntityClass<DocumentEntity>(DocumentTable)

    var name by DocumentTable.name
    var contentType by DocumentTable.contentType
    var data by DocumentTable.data

    @OptIn(InternalAPI::class)
    override suspend fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }
}

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<IdentityEntity>(IdentityTable)

    var authority by IdentityTable.authority
    var email by IdentityTable.email
    var password by IdentityTable.password
    var status by IdentityTable.status
    var phone by IdentityTable.phone
    var telegram by IdentityTable.telegram
    var whatsapp by IdentityTable.whatsapp
    var max by IdentityTable.max
    var firstname by IdentityTable.firstname
    var lastname by IdentityTable.lastname
    var patronymic by IdentityTable.patronymic
    var about by IdentityTable.about
    var image by ImageEntity.optionalReferencedOn(IdentityTable.imageId)
    val portfolio by PortfolioEntity.optionalBackReferencedOn(PortfolioTable.identityId)
}

class ProfessionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfessionEntity>(ProfessionTable)

    var name by ProfessionTable.name
    var description by ProfessionTable.description
    val portfolios by PortfolioEntity.optionalReferrersOn(PortfolioTable.professionId)
}

class PortfolioEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PortfolioEntity>(PortfolioTable)

    var profession by ProfessionEntity.optionalReferencedOn(PortfolioTable.professionId)
    var identity by IdentityEntity.referencedOn(PortfolioTable.identityId)
    var description by PortfolioTable.description
    var createdAt by PortfolioTable.createdAt
    val projects by ProjectEntity.referrersOn(ProjectTable.portfolioId)
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectTable)

    var name by ProjectTable.name
    var description by ProjectTable.description
    var portfolio by PortfolioEntity.referencedOn(ProjectTable.portfolioId)
    var link by ProjectTable.link
    var createdAt by ProjectTable.createdAt
    var image by ImageEntity.optionalReferencedOn(ProjectTable.imageId)
    var videos by VideoEntity.via(ProjectVideoTable.projectId, ProjectVideoTable.videoId)
    var documents by DocumentEntity.via(ProjectDocumentTable.projectId, ProjectDocumentTable.documentId)
}
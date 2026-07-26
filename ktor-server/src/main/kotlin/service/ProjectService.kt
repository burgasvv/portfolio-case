package org.burgas.service

import io.ktor.http.content.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.burgas.dao.ProjectEntity
import org.burgas.database.*
import org.burgas.dto.ProjectFileRequest
import org.burgas.dto.ProjectRequest
import org.burgas.dto.ProjectResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.SizedCollection
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class ProjectService : ReadService<UUID, ProjectEntity>, FindService<UUID, ProjectResponse>,
    CreateService<ProjectRequest, ProjectResponse>, UpdateService<ProjectRequest, ProjectResponse>,
    DeleteService<UUID> {

    private val imageService: ImageService
    private val videoService: VideoService
    private val documentService: DocumentService

    constructor(imageService: ImageService, videoService: VideoService, documentService: DocumentService) {
        this.imageService = imageService
        this.videoService = videoService
        this.documentService = documentService
    }

    override suspend fun readEntity(id: UUID): ProjectEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        ProjectEntity[id]
            .load(
                ProjectEntity::portfolio,
                ProjectEntity::image, ProjectEntity::videos, ProjectEntity::documents
            )
    }

    override suspend fun findById(id: UUID): ProjectResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        readEntity(id).toResponse()
    }

    override suspend fun create(request: ProjectRequest): ProjectResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProjectEntity.new { this.create(request) }
            .load(
                ProjectEntity::portfolio,
                ProjectEntity::image, ProjectEntity::videos, ProjectEntity::documents
            ).toResponse()
    }

    override suspend fun update(request: ProjectRequest): ProjectResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProjectEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(
                ProjectEntity::portfolio,
                ProjectEntity::image, ProjectEntity::videos, ProjectEntity::documents
            ).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val projectEntity = readEntity(id)
        projectEntity.image?.delete()
        projectEntity.videos.forEach { it.delete() }
        projectEntity.documents.forEach { it.delete() }
        projectEntity.delete()
    }

    suspend fun uploadImage(projectId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val projectEntity = readEntity(projectId)
        require(projectEntity.image == null) { "Project image already set" }
        val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
        projectEntity.image = imageService.upload(fileItem)
    }

    suspend fun removeImage(projectId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val projectEntity = readEntity(projectId)
        projectEntity.image!!.delete()
    }

    suspend fun uploadVideos(projectId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val projectEntity = readEntity(projectId)
        val videoEntities = multiPartData.asFlow().filterIsInstance<PartData.FileItem>()
            .map { videoService.upload(it) }.toList()
        projectEntity.videos = SizedCollection(projectEntity.videos + videoEntities)
    }

    suspend fun removeVideos(projectFileRequest: ProjectFileRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProjectVideoTable.deleteWhere {
            ProjectVideoTable.projectId eq projectFileRequest.projectId
            ProjectVideoTable.videoId inList projectFileRequest.fileIds
        }
        VideoTable.deleteWhere { VideoTable.id inList projectFileRequest.fileIds }
    }

    suspend fun uploadDocuments(projectId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val projectEntity = readEntity(projectId)
        val documentEntities = multiPartData.asFlow().filterIsInstance<PartData.FileItem>()
            .map { documentService.upload(it) }.toList()
        projectEntity.documents = SizedCollection(projectEntity.documents + documentEntities)
    }

    suspend fun removeDocuments(projectFileRequest: ProjectFileRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProjectDocumentTable.deleteWhere {
            ProjectDocumentTable.projectId eq projectFileRequest.projectId
            ProjectDocumentTable.documentId inList projectFileRequest.fileIds
        }
        DocumentTable.deleteWhere { DocumentTable.id inList projectFileRequest.fileIds }
    }
}
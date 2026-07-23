package org.burgas.service

import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.asFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.burgas.dao.IdentityEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

class IdentityService : CollectService<IdentityResponse>, ReadService<UUID, IdentityEntity>,
    FindService<UUID, IdentityResponse>, CreateService<IdentityRequest, IdentityResponse>,
    UpdateService<IdentityRequest, IdentityResponse>, DeleteService<UUID> {

    private val imageService: ImageService

    constructor(imageService: ImageService) {
        this.imageService = imageService
    }

    override suspend fun findAll(): List<IdentityResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.all().with(IdentityEntity::image, IdentityEntity::portfolio).map { it.toResponse() }
    }

    override suspend fun readEntity(id: UUID): IdentityEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.findById(id)!!.load(IdentityEntity::image, IdentityEntity::portfolio)
    }

    override suspend fun findById(id: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        readEntity(id).toResponse()
    }

    override suspend fun create(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        IdentityEntity.new { this.create(request) }
            .load(IdentityEntity::image, IdentityEntity::portfolio).toResponse()
    }

    suspend fun createAdmin(identityRequest: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = IdentityEntity.new { this.create(identityRequest) }
            .load(IdentityEntity::image, IdentityEntity::portfolio)
        identityEntity.authority = Authority.ADMIN
        identityEntity.toResponse()
    }

    override suspend fun update(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        IdentityEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(IdentityEntity::image, IdentityEntity::portfolio).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = readEntity(id)
        identityEntity.image?.delete()
        identityEntity.delete()
    }

    suspend fun changePassword(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = readEntity(identityRequest.id!!)
        if (!BCrypt.checkpw(identityRequest.password!!, identityEntity.password)) {
            identityEntity.password = BCrypt.hashpw(identityRequest.password, BCrypt.gensalt())
        } else {
            throw IllegalArgumentException("Passwords matched")
        }
    }

    suspend fun changeStatus(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = readEntity(identityRequest.id!!)
        if (identityEntity.status != identityRequest.status!!) {
            identityEntity.status = identityRequest.status
        } else {
            throw IllegalArgumentException("Statuses matched")
        }
    }

    suspend fun uploadImage(identityId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = readEntity(identityId)
        if (identityEntity.image == null) {
            val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
            identityEntity.image = imageService.upload(fileItem)
        } else {
            throw IllegalArgumentException("Identity image already set")
        }
    }

    suspend fun removeImage(identityId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = readEntity(identityId)
        val imageEntity = identityEntity.image
        if (imageEntity != null) {
            imageEntity.delete()
        } else {
            throw IllegalArgumentException("Identity image is null for delete")
        }
    }
}
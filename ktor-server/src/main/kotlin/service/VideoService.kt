package org.burgas.service

import io.ktor.http.content.*
import org.burgas.dao.VideoEntity
import org.burgas.database.DatabaseConnection
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UploadService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class VideoService : ReadService<UUID, VideoEntity>, UploadService<VideoEntity>, DeleteService<UUID> {

    override suspend fun readEntity(id: UUID): VideoEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        VideoEntity.findById(id)!!
    }

    override suspend fun upload(fileItem: PartData.FileItem): VideoEntity = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        VideoEntity.new { this.upload(fileItem) }
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        readEntity(id).delete()
    }
}
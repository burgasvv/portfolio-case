package org.burgas.service

import io.ktor.http.content.PartData
import org.burgas.dao.DocumentEntity
import org.burgas.database.DatabaseConnection
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UploadService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.UUID

class DocumentService : ReadService<UUID, DocumentEntity>, UploadService<DocumentEntity>, DeleteService<UUID> {

    override suspend fun readEntity(id: UUID): DocumentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        DocumentEntity.findById(id)!!
    }

    override suspend fun upload(fileItem: PartData.FileItem): DocumentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        DocumentEntity.new { this.upload(fileItem) }
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        readEntity(id).delete()
    }
}
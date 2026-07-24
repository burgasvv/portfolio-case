package org.burgas.service

import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.burgas.dao.ProfessionEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.ProfessionRequest
import org.burgas.dto.ProfessionResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class ProfessionService : CollectService<ProfessionResponse>, ReadService<UUID, ProfessionEntity>,
    FindService<UUID, ProfessionResponse>, CreateService<ProfessionRequest, ProfessionResponse>,
    UpdateService<ProfessionRequest, ProfessionResponse>, DeleteService<UUID> {

    override suspend fun findAll(): List<ProfessionResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        ProfessionEntity.all().with(ProfessionEntity::portfolios).map { it.toResponse() }
    }

    override suspend fun readEntity(id: UUID): ProfessionEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        ProfessionEntity.findById(id)!!.load(ProfessionEntity::portfolios)
    }

    override suspend fun findById(id: UUID): ProfessionResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        readEntity(id).toResponse()
    }

    override suspend fun create(request: ProfessionRequest): ProfessionResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProfessionEntity.new { this.create(request) }.load(ProfessionEntity::portfolios).toResponse()
    }

    override suspend fun update(request: ProfessionRequest): ProfessionResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ProfessionEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(ProfessionEntity::portfolios).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        readEntity(id).delete()
    }

    @OptIn(InternalAPI::class)
    suspend fun createByDocument(multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
        XSSFWorkbook(fileItem.provider().readBuffer.inputStream()).use { workbook ->
            workbook.getSheetAt(0).forEach { row ->
                val professionRequest = ProfessionRequest(
                    id = UUID.randomUUID(),
                    name = row.getCell(0)!!.stringCellValue,
                    description = row.getCell(1)!!.stringCellValue
                )
                ProfessionEntity.new { this.create(professionRequest) }
            }
        }
    }
}
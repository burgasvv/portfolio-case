package org.burgas.service

import org.burgas.dao.PortfolioEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.PortfolioRequest
import org.burgas.dto.PortfolioResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class PortfolioService : CollectService<PortfolioResponse>, ReadService<UUID, PortfolioEntity>,
    FindService<UUID, PortfolioResponse>, CreateService<PortfolioRequest, PortfolioResponse>,
    UpdateService<PortfolioRequest, PortfolioResponse>, DeleteService<UUID> {

    override suspend fun findAll(): List<PortfolioResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        PortfolioEntity.all()
            .with(PortfolioEntity::identity, PortfolioEntity::profession, PortfolioEntity::projects)
            .map { it.toResponse() }
    }

    override suspend fun readEntity(id: UUID): PortfolioEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        PortfolioEntity.findById(id)!!
            .load(PortfolioEntity::identity, PortfolioEntity::profession, PortfolioEntity::projects)
    }

    override suspend fun findById(id: UUID): PortfolioResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        readEntity(id).toResponse()
    }

    override suspend fun create(request: PortfolioRequest): PortfolioResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        PortfolioEntity.new { this.create(request) }
            .load(PortfolioEntity::identity, PortfolioEntity::profession, PortfolioEntity::projects)
            .toResponse()
    }

    override suspend fun update(request: PortfolioRequest): PortfolioResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        PortfolioEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(PortfolioEntity::identity, PortfolioEntity::profession, PortfolioEntity::projects)
            .toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        readEntity(id).delete()
    }
}
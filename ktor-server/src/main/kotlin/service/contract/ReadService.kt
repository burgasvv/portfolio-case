package org.burgas.service.contract

import org.jetbrains.exposed.v1.dao.java.UUIDEntity

interface ReadService<in ID, out E : UUIDEntity> {

    suspend fun readEntity(id: ID): E
}
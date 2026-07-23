package org.burgas.service.contract

import io.ktor.http.content.MultiPartData

interface HandleFile<in EntityID> {

    suspend fun upload(entityID: EntityID, multiPartData: MultiPartData)

    suspend fun remove(entityID: EntityID)
}
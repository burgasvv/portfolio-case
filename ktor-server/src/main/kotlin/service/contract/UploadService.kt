package org.burgas.service.contract

import io.ktor.http.content.PartData
import org.burgas.dao.File

interface UploadService<out F : File> {

    suspend fun upload(fileItem: PartData.FileItem): F
}
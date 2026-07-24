package org.burgas.dao

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.burgas.database.*
import org.burgas.dto.Dependency
import org.burgas.dto.DocumentDependency
import org.burgas.dto.IdentityDependency
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.dto.ImageDependency
import org.burgas.dto.PortfolioDependencyInIdentity
import org.burgas.dto.PortfolioDependencyInProfession
import org.burgas.dto.PortfolioDependencyInProject
import org.burgas.dto.PortfolioRequest
import org.burgas.dto.PortfolioResponse
import org.burgas.dto.ProfessionDependency
import org.burgas.dto.ProfessionRequest
import org.burgas.dto.ProfessionResponse
import org.burgas.dto.ProjectDependency
import org.burgas.dto.ProjectRequest
import org.burgas.dto.ProjectResponse
import org.burgas.dto.Request
import org.burgas.dto.Response
import org.burgas.dto.VideoDependency
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.mindrot.jbcrypt.BCrypt
import java.util.*

interface File

interface Dao

interface Uploader {
    fun upload(fileItem: PartData.FileItem)
}

interface Creator<R : Request> {
    fun create(request: R)
}

interface Modifier<R : Request> {
    fun update(request: R)
}

interface DependencyMapper<out D : Dependency> {
    suspend fun toDependency(): D
}

interface ResponseMapper<out R : Response> {
    suspend fun toResponse(): R
}

class ImageEntity(id: EntityID<UUID>) : UUIDEntity(id), File, Uploader, DependencyMapper<ImageDependency> {
    companion object : UUIDEntityClass<ImageEntity>(ImageTable)

    var name by ImageTable.name
    var contentType by ImageTable.contentType
    var preview by ImageTable.preview
    var data by ImageTable.data

    @OptIn(InternalAPI::class)
    override fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.preview = true
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }

    override suspend fun toDependency(): ImageDependency {
        return ImageDependency(
            id = this.id.value,
            name = this.name,
            contentType = this.contentType,
            preview = this.preview
        )
    }
}

class VideoEntity(id: EntityID<UUID>) : UUIDEntity(id), File, Uploader, DependencyMapper<VideoDependency> {
    companion object : UUIDEntityClass<VideoEntity>(VideoTable)

    var name by VideoTable.name
    var contentType by VideoTable.contentType
    var data by VideoTable.data

    @OptIn(InternalAPI::class)
    override fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }

    override suspend fun toDependency(): VideoDependency {
        return VideoDependency(
            id = this.id.value,
            name = this.name,
            contentType = this.contentType
        )
    }
}

class DocumentEntity(id: EntityID<UUID>) : UUIDEntity(id), File, Uploader, DependencyMapper<DocumentDependency> {
    companion object : UUIDEntityClass<DocumentEntity>(DocumentTable)

    var name by DocumentTable.name
    var contentType by DocumentTable.contentType
    var data by DocumentTable.data

    @OptIn(InternalAPI::class)
    override fun upload(fileItem: PartData.FileItem) {
        this.name = fileItem.originalFileName!!
        this.contentType = "${fileItem.contentType!!.contentType}/${fileItem.contentType!!.contentSubtype}"
        this.data = ExposedBlob(fileItem.provider().readBuffer.readByteArray())
    }

    override suspend fun toDependency(): DocumentDependency {
        return DocumentDependency(
            id = this.id.value,
            name = this.name,
            contentType = this.contentType
        )
    }
}

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<IdentityRequest>, Modifier<IdentityRequest>,
    DependencyMapper<IdentityDependency>, ResponseMapper<IdentityResponse> {
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

    override fun create(request: IdentityRequest) {
        request.email.takeUnless { it.isNullOrEmpty() }!!.let { this.email = it }
        request.password.takeUnless { it.isNullOrEmpty() }!!.let { this.password = BCrypt.hashpw(it, BCrypt.gensalt()) }
        request.phone.takeUnless { it.isNullOrEmpty() }!!.let { this.phone = it }
        request.telegram.takeUnless { it.isNullOrEmpty() }?.let { this.telegram = it }
        request.whatsapp.takeUnless { it.isNullOrEmpty() }?.let { this.whatsapp = it }
        request.max.takeUnless { it.isNullOrEmpty() }?.let { this.max = it }
        request.firstname.takeUnless { it.isNullOrEmpty() }!!.let { this.firstname = it }
        request.lastname.takeUnless { it.isNullOrEmpty() }!!.let { this.lastname = it }
        request.patronymic.takeUnless { it.isNullOrEmpty() }!!.let { this.patronymic = it }
        request.about.takeUnless { it.isNullOrEmpty() }?.let { this.about = it }
    }

    override fun update(request: IdentityRequest) {
        request.email.takeUnless { it.isNullOrEmpty() }?.let { this.email = it }
        request.phone.takeUnless { it.isNullOrEmpty() }?.let { this.phone = it }
        request.telegram.takeUnless { it.isNullOrEmpty() }?.let { this.telegram = it }
        request.whatsapp.takeUnless { it.isNullOrEmpty() }?.let { this.whatsapp = it }
        request.max.takeUnless { it.isNullOrEmpty() }?.let { this.max = it }
        request.firstname.takeUnless { it.isNullOrEmpty() }?.let { this.firstname = it }
        request.lastname.takeUnless { it.isNullOrEmpty() }?.let { this.lastname = it }
        request.patronymic.takeUnless { it.isNullOrEmpty() }?.let { this.patronymic = it }
        request.about.takeUnless { it.isNullOrEmpty() }?.let { this.about = it }
    }

    override suspend fun toDependency(): IdentityDependency {
        return IdentityDependency(
            id = this.id.value,
            authority = this.authority,
            email = this.email,
            status = this.status,
            phone = this.phone,
            telegram = this.telegram,
            whatsapp = this.whatsapp,
            max = this.max,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic,
            about = this.about,
            image = this.image?.toDependency()
        )
    }

    override suspend fun toResponse(): IdentityResponse {
        return IdentityResponse(
            id = this.id.value,
            authority = this.authority,
            email = this.email,
            status = this.status,
            phone = this.phone,
            telegram = this.telegram,
            whatsapp = this.whatsapp,
            max = this.max,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic,
            about = this.about,
            image = this.image?.toDependency(),
            portfolio = this.portfolio?.toPortfolioDependencyInIdentity()
        )
    }
}

class ProfessionEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<ProfessionRequest>, Modifier<ProfessionRequest>,
    DependencyMapper<ProfessionDependency>, ResponseMapper<ProfessionResponse> {
    companion object : UUIDEntityClass<ProfessionEntity>(ProfessionTable)

    var name by ProfessionTable.name
    var description by ProfessionTable.description
    val portfolios by PortfolioEntity.optionalReferrersOn(PortfolioTable.professionId)

    override fun create(request: ProfessionRequest) {
        request.name.takeUnless { it.isNullOrEmpty() }!!.let { this.name = it }
        request.description.takeUnless { it.isNullOrEmpty() }!!.let { this.description = it }
    }

    override fun update(request: ProfessionRequest) {
        request.name.takeUnless { it.isNullOrEmpty() }?.let { this.name = it }
        request.description.takeUnless { it.isNullOrEmpty() }?.let { this.description = it }
    }

    override suspend fun toDependency(): ProfessionDependency {
        return ProfessionDependency(
            id = this.id.value,
            name = this.name,
            description = this.description
        )
    }

    override suspend fun toResponse(): ProfessionResponse {
        return ProfessionResponse(
            id = this.id.value,
            name = this.name,
            description = this.description,
            portfolios = this.portfolios.map { it.toPortfolioDependencyInProfession() }
        )
    }
}

class PortfolioEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<PortfolioRequest>,
    Modifier<PortfolioRequest>, ResponseMapper<PortfolioResponse> {
    companion object : UUIDEntityClass<PortfolioEntity>(PortfolioTable)

    var profession by ProfessionEntity.optionalReferencedOn(PortfolioTable.professionId)
    var identity by IdentityEntity.referencedOn(PortfolioTable.identityId)
    var description by PortfolioTable.description
    var createdAt by PortfolioTable.createdAt
    val projects by ProjectEntity.referrersOn(ProjectTable.portfolioId)

    override fun create(request: PortfolioRequest) {
        request.professionId!!.let { this.profession = ProfessionEntity.findById(it)!! }
        request.identityId!!.let { this.identity = IdentityEntity.findById(it)!! }
        request.description.takeUnless { it.isNullOrEmpty() }?.let { this.description = it }
    }

    override fun update(request: PortfolioRequest) {
        request.professionId?.let { this.profession = ProfessionEntity.findById(it)!! }
        request.description.takeUnless { it.isNullOrEmpty() }?.let { this.description = it }
    }

    suspend fun toPortfolioDependencyInIdentity(): PortfolioDependencyInIdentity {
        return PortfolioDependencyInIdentity(
            id = this.id.value,
            profession = this.profession?.toDependency(),
            description = this.description,
            createdAt = this.createdAt
        )
    }

    suspend fun toPortfolioDependencyInProfession(): PortfolioDependencyInProfession {
        return PortfolioDependencyInProfession(
            id = this.id.value,
            identity = this.identity.toDependency(),
            description = this.description,
            createdAt = this.createdAt
        )
    }

    suspend fun toPortfolioDependencyInProject(): PortfolioDependencyInProject {
        return PortfolioDependencyInProject(
            id = this.id.value,
            profession = this.profession?.toDependency(),
            identity = this.identity.toDependency(),
            description = this.description,
            createdAt = this.createdAt
        )
    }

    override suspend fun toResponse(): PortfolioResponse {
        return PortfolioResponse(
            id = this.id.value,
            profession = this.profession?.toDependency(),
            identity = this.identity.toDependency(),
            description = this.description,
            createdAt = this.createdAt,
            projects = this.projects.map { it.toDependency() }
        )
    }
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<ProjectRequest>, Modifier<ProjectRequest>,
    DependencyMapper<ProjectDependency>, ResponseMapper<ProjectResponse> {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectTable)

    var name by ProjectTable.name
    var description by ProjectTable.description
    var portfolio by PortfolioEntity.referencedOn(ProjectTable.portfolioId)
    var link by ProjectTable.link
    var createdAt by ProjectTable.createdAt
    var image by ImageEntity.optionalReferencedOn(ProjectTable.imageId)
    var videos by VideoEntity.via(ProjectVideoTable.projectId, ProjectVideoTable.videoId)
    var documents by DocumentEntity.via(ProjectDocumentTable.projectId, ProjectDocumentTable.documentId)

    override fun create(request: ProjectRequest) {
        request.name.takeUnless { it.isNullOrEmpty() }!!.let { this.name = it }
        request.description.takeUnless { it.isNullOrEmpty() }?.let { this.description = it }
        request.portfolioId!!.let { this.portfolio = PortfolioEntity.findById(it)!! }
        request.link.takeUnless { it.isNullOrEmpty() }?.let { this.link = it }
    }

    override fun update(request: ProjectRequest) {
        request.name.takeUnless { it.isNullOrEmpty() }?.let { this.name = it }
        request.description.takeUnless { it.isNullOrEmpty() }?.let { this.description = it }
        request.link.takeUnless { it.isNullOrEmpty() }?.let { this.link = it }
    }

    override suspend fun toDependency(): ProjectDependency {
        return ProjectDependency(
            id = this.id.value,
            name = this.name,
            description = this.description,
            createdAt = this.createdAt,
            image = this.image?.toDependency()
        )
    }

    override suspend fun toResponse(): ProjectResponse {
        return ProjectResponse(
            id = this.id.value,
            name = this.name,
            description = this.description,
            portfolio = this.portfolio.toPortfolioDependencyInProject(),
            link = this.link,
            createdAt = this.createdAt,
            image = this.image?.toDependency(),
            videos = this.videos.map { it.toDependency() },
            documents = this.documents.map { it.toDependency() }
        )
    }
}
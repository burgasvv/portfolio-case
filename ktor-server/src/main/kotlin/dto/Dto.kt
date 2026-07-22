package org.burgas.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.burgas.database.Authority
import java.util.*

interface Request {
    val id: UUID?
}

interface Dependency {
    val id: UUID
}

interface Response {
    val id: UUID
}

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String?
)

@Serializable
data class ImageDependency(
    @Contextual
    override val id: UUID,
    val name: String,
    val contentType: String,
    val preview: Boolean
) : Dependency

@Serializable
data class VideoDependency(
    @Contextual
    override val id: UUID,
    val name: String,
    val contentType: String
) : Dependency

@Serializable
data class DocumentDependency(
    @Contextual
    override val id: UUID,
    val name: String,
    val contentType: String
) : Dependency

@Serializable
data class IdentityRequest(
    @Contextual
    override val id: UUID?,
    val authority: Authority?,
    val email: String?,
    val password: String?,
    val status: Boolean?,
    val phone: String?,
    val telegram: String?,
    val whatsapp: String?,
    val max: String?,
    val firstname: String?,
    val lastname: String?,
    val patronymic: String?,
    val about: String?
) : Request

@Serializable
data class IdentityDependency(
    @Contextual
    override val id: UUID,
    val authority: Authority,
    val email: String,
    val status: Boolean,
    val phone: String,
    val telegram: String?,
    val whatsapp: String?,
    val max: String?,
    val firstname: String,
    val lastname: String,
    val patronymic: String,
    val about: String?,
    val image: ImageDependency?
) : Dependency

@Serializable
data class IdentityResponse(
    @Contextual
    override val id: UUID,
    val authority: Authority,
    val email: String,
    val status: Boolean,
    val phone: String,
    val telegram: String?,
    val whatsapp: String?,
    val max: String?,
    val firstname: String,
    val lastname: String,
    val patronymic: String,
    val about: String?,
    val image: ImageDependency?,
    val portfolio: PortfolioDependencyInIdentity?
) : Response

@Serializable
data class ProfessionRequest(
    @Contextual
    override val id: UUID?,
    val name: String?,
    val description: String?
) : Request

@Serializable
data class ProfessionDependency(
    @Contextual
    override val id: UUID,
    val name: String,
    val description: String
) : Dependency

@Serializable
data class ProfessionResponse(
    @Contextual
    override val id: UUID,
    val name: String,
    val description: String,
    val portfolios: List<PortfolioDependencyInProfession>?
) : Response

@Serializable
data class PortfolioRequest(
    @Contextual
    override val id: UUID?,
    val professionId: UUID?,
    val identityId: UUID?,
    val description: String?
) : Request

@Serializable
data class PortfolioDependencyInIdentity(
    @Contextual
    override val id: UUID,
    val profession: ProfessionDependency?,
    val description: String?,
    val createdAt: LocalDateTime
) : Dependency

@Serializable
data class PortfolioDependencyInProfession(
    @Contextual
    override val id: UUID,
    val identity: IdentityDependency,
    val description: String?,
    val createdAt: LocalDateTime
) : Dependency

@Serializable
data class PortfolioDependencyInProject(
    @Contextual
    override val id: UUID,
    val profession: ProfessionDependency?,
    val identity: IdentityDependency,
    val description: String?,
    val createdAt: LocalDateTime
) : Dependency

@Serializable
data class PortfolioResponse(
    @Contextual
    override val id: UUID,
    val profession: ProfessionDependency?,
    val identity: IdentityDependency,
    val description: String?,
    val createdAt: LocalDateTime,
    val projects: List<ProjectDependency>
) : Response

@Serializable
data class ProjectRequest(
    @Contextual
    override val id: UUID?,
    val name: String?,
    val description: String?,
    val portfolioId: UUID?,
    val link: String?
) : Request

@Serializable
data class ProjectDependency(
    @Contextual
    override val id: UUID,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val image: ImageDependency?
) : Dependency

@Serializable
data class ProjectResponse(
    @Contextual
    override val id: UUID,
    val name: String,
    val description: String?,
    val portfolio: PortfolioDependencyInProject,
    val link: String?,
    val createdAt: LocalDateTime,
    val image: ImageDependency?,
    val videos: List<VideoDependency>,
    val documents: List<DocumentDependency>
) : Response
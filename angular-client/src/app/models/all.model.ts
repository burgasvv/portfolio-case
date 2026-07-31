
export interface AuthRequest {
    email: string
    password: string
}

export interface ImageDependency {
    id: string
    name: string
    contentType: string
    preview: boolean
}

export interface VideoDependency {
    id: string
    name: string
    contentType: string
}

export interface DocumentDependency {
    id: string
    name: string
    contentType: string
}

export enum Authority {
    ADMIN = "ADMIN",
    USER = "USER"
}

export interface IdentityRequest {
    id?: string
    authority?: Authority
    email?: string
    password?: string
    status?: boolean
    phone?: string
    telegram?: string
    whatsapp?: string
    max?: string
    firstname?: string
    lastname?: string
    patronymic?: string
    about?: string
}

export interface IdentityDependency {
    id: string
    authority: Authority
    email: string
    status: boolean
    phone: string
    telegram: string | null
    whatsapp: string | null
    max: string | null
    firstname: string
    lastname: string
    patronymic: string
    about: string | null
    image: ImageDependency | null
}

export interface IdentityResponse {
    id: string
    authority: Authority
    email: string
    status: boolean
    phone: string
    telegram: string | null
    whatsapp: string | null
    max: string | null
    firstname: string
    lastname: string
    patronymic: string
    about: string | null
    image: ImageDependency | null
    portfolio: PortfolioDependencyInIdentity | null
}

export interface ProfessionRequest {
    id?: string
    name?: string
    description?: string
}

export interface ProfessionDependency {
    id: string
    name: string
    description: string
}

export interface ProfessionResponse {
    id: string
    name: string
    description: string
    portfolios: PortfolioDependencyInProfession[] | null
}

export interface PortfolioRequest {
    id?: string
    professionId?: string
    identityId?: string
    description?: string
}

export interface PortfolioDependencyInIdentity {
    id: string
    profession: ProfessionDependency | null
    description: string | null
    createdAt: Date
}

export interface PortfolioDependencyInProfession {
    id: string
    identity: IdentityDependency
    description: string | null
    createdAt: Date
}

export interface PortfolioDependencyInProject {
    id: string
    profession: ProfessionDependency | null
    identity: IdentityDependency
    description: string | null
    createdAt: Date
}

export interface PortfolioResponse {
    id: string
    profession: ProfessionDependency | null
    identity: IdentityDependency
    description: string | null
    createdAt: Date
    projects: ProjectDependency[]
}

export interface ProjectFileRequest {
    projectId: string
    fileIds: string[]
}

export interface ProjectRequest {
    id?: string
    name?: string
    description?: string
    portfolioId?: string
    link?: string
}

export interface ProjectDependency {
    id: string
    name: string
    description: string | null
    createdAt: Date
    image: ImageDependency | null
}

export interface ProjectResponse {
    id: string
    name: string
    description: string | null
    portfolio: PortfolioDependencyInProject
    link: string | null
    createdAt: Date
    image: ImageDependency | null
    videos: VideoDependency[]
    documents: DocumentDependency[]
}

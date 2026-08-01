import {inject, Input, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {IdentityRequest, IdentityResponse} from '../../models/all.model';

@Service()
export class IdentityService {

    private readonly url: string = "http://localhost:9000/api/v1/identities"
    private readonly http = inject(HttpClient)

    findAll(): Observable<IdentityResponse[]> {
        return this.http.get<IdentityResponse[]>(`${this.url}`, {withCredentials: true})
    }

    findById(id: string): Observable<IdentityResponse> {
        return this.http.get<IdentityResponse>(`${this.url}/by-id?identityId=${id}`, {withCredentials: true})
    }

    create(identityRequest: IdentityRequest): Observable<IdentityResponse> {
        return this.http.post<IdentityResponse>(`${this.url}/create`, identityRequest)
    }

    update(identityRequest: IdentityRequest): Observable<IdentityResponse> {
        return this.http.put<IdentityResponse>(`${this.url}/update`, identityRequest, {withCredentials: true})
    }

    delete(id: string): Observable<any> {
        return this.http.delete(`${this.url}/delete?identityId=${id}`, {withCredentials: true})
    }
}

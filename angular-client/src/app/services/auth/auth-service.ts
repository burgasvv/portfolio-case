import {inject, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {IdentityResponse} from '../../models/all.model';

@Service()
export class AuthService {

    private readonly url: string = "http://localhost:9000/api/v1/security"
    private readonly http = inject(HttpClient)

    authenticated(): Observable<IdentityResponse> {
        return this.http.get<IdentityResponse>(`${this.url}/authenticated`, { withCredentials: true })
    }

    login(credentials: { email: string; password: string; }): Observable<IdentityResponse> {
        return this.http.post<IdentityResponse>(`${this.url}/login`, credentials)
    }

    logout(): Observable<any> {
        return this.http.post(`${this.url}/logout`, {}, { withCredentials: true })
    }
}

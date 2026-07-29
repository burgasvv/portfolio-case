import {computed, inject, Service, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {IdentityResponse} from '../../models/all.model';

@Service()
export class AuthService {

    private readonly url: string = "http://localhost:9000/api/v1/security"
    private readonly http = inject(HttpClient)
    readonly authenticatedUser = signal<IdentityResponse | null>(null)
    readonly isAuthenticated = computed(() => !!this.authenticatedUser())

    constructor() {
        this.checkInitialAuth();
    }

    private checkInitialAuth(): void {
        this.getAuthenticated().subscribe({
            next: (user) => this.authenticatedUser.set(user),
            error: () => this.authenticatedUser.set(null)
        })
    }

    private getAuthenticated(): Observable<IdentityResponse> {
        return this.http.get<IdentityResponse>(`${this.url}/authenticated`, { withCredentials: true })
    }

    login(authRequest: { email: string; password: string; }): Observable<IdentityResponse> {
        return this.http.post<IdentityResponse>(`${this.url}/login`, authRequest, { withCredentials: true })
    }

    logout(): Observable<any> {
        let token = localStorage.getItem("AUTH_TOKEN");
        const headers = {'Authorization': `Bearer ${token}`};
        return this.http.post(`${this.url}/logout`, {}, { headers, withCredentials: true })
    }
}

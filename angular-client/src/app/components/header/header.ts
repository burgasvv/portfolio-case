import {Component, inject, NgZone} from '@angular/core';
import {AuthService} from '../../services/auth/auth-service';
import {Router} from '@angular/router';

@Component({
    selector: 'app-header',
    standalone: false,
    templateUrl: './header.html',
    styleUrl: './header.scss',
})
export class Header {

    protected readonly authService = inject(AuthService)
    private readonly router = inject(Router);
    private readonly ngZone = inject(NgZone);

    onLogout(): void {
        this.authService.logout().subscribe({
            next: () => this.handleLocalLogout(),
            error: (err) => {
                console.error('Ошибка бэкенда при logout:', err);
                this.handleLocalLogout();
            }
        });
    }

    private handleLocalLogout(): void {
        this.ngZone.run(() => {
            this.authService.authenticatedUser.set(null);
            this.router.navigate(['/login']).then(r => r)
        });
    }
}

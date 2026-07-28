import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth/auth-service';
import {Router} from '@angular/router';

@Component({
    selector: 'app-logout',
    standalone: false,
    templateUrl: './logout.html',
    styleUrl: './logout.scss',
})
export class Logout implements OnInit {

    private readonly authService = inject(AuthService)
    private readonly router = inject(Router)

    ngOnInit(): void {
        this.authService.logout()
        this.router.navigate(['/login'])
    }
}

import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth/auth-service';
import {NgForm} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthRequest} from '../../models/all.model';

@Component({
    selector: 'app-login',
    standalone: false,
    templateUrl: './login.html',
    styleUrl: './login.scss',
})
export class Login {

    protected readonly authService = inject(AuthService)
    private readonly router = inject(Router)
    errorMessage: string | null = null

    onSubmit(form: NgForm) {
        if (form.invalid) return
        const authRequest: AuthRequest = {
            email: form.value.email,
            password: form.value.password
        };
        this.authService.login(authRequest).subscribe({
            next: (user) => {
                this.authService.authenticatedUser.set(user);
                this.router.navigate(['']).then(r => r);
            },
            error: (err) => {
                if (err.status === 400) {
                    this.errorMessage = "Ошибка аутентификации."
                } else {
                    this.errorMessage = 'Что-то пошло не так. Попробуйте позже.'
                }
            }
        });
    }
}

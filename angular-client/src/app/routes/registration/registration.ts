import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth/auth-service';
import {Authority} from '../../models/all.model';
import {IdentityService} from '../../services/identity/identity-service';
import {NgForm} from '@angular/forms';
import {Router} from '@angular/router';

@Component({
    selector: 'app-registration',
    standalone: false,
    templateUrl: './registration.html',
    styleUrl: './registration.scss',
})
export class Registration {

    protected readonly authService = inject(AuthService)
    protected readonly Authority = Authority
    private readonly identityService = inject(IdentityService)
    private readonly router = inject(Router)
    errorMessage: string | null = null

    submitByAdmin(form: NgForm) {
        if (form.invalid) return
        const identityRequest = {
            email: form.value.email,
            password: form.value.password,
            phone: form.value.phone,
            telegram: form.value.telegram,
            whatsapp: form.value.whatsapp,
            max: form.value.max,
            firstname: form.value.firstname,
            lastname: form.value.lastname,
            patronymic: form.value.patronymic,
            about: form.value.about
        }
        this.identityService.create(identityRequest).subscribe({
            error: err => {
                if (err.status === 400) {
                    this.errorMessage = "Ошибка регистрации."
                } else {
                    this.errorMessage = 'Что-то пошло не так. Попробуйте позже.'
                }
            }
        })
    }

    submitByGuest(form: NgForm) {
        if (form.invalid) return
        const identityRequest = {
            email: form.value.email,
            password: form.value.password,
            phone: form.value.phone,
            telegram: form.value.telegram,
            whatsapp: form.value.whatsapp,
            max: form.value.max,
            firstname: form.value.firstname,
            lastname: form.value.lastname,
            patronymic: form.value.patronymic,
            about: form.value.about
        }
        this.identityService.create(identityRequest).subscribe({
            next: value => {
                const authRequest = {
                    email: value.email,
                    password: identityRequest.password
                }
                this.authService.login(authRequest).subscribe({
                    next: (user) => {
                        this.authService.authenticatedUser.set(user);
                        this.router.navigate(['identities/by-id'], {queryParams: {identityId: user.id}}).then(r => r)
                    }
                })
            },
            error: err => {
                if (err.status === 400) {
                    this.errorMessage = "Ошибка регистрации."
                } else {
                    this.errorMessage = 'Что-то пошло не так. Попробуйте позже.'
                }
            }
        })
    }
}

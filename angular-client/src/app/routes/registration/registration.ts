import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth/auth-service';
import {Authority} from '../../models/all.model';

@Component({
    selector: 'app-registration',
    standalone: false,
    templateUrl: './registration.html',
    styleUrl: './registration.scss',
})
export class Registration {

    protected readonly authService = inject(AuthService)
    protected readonly Authority = Authority;
}

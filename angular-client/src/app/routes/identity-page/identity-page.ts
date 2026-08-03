import {Component, inject, Input, OnInit, signal} from '@angular/core';
import {IdentityService} from '../../services/identity/identity-service';
import {Authority, IdentityResponse} from '../../models/all.model';
import {AuthService} from '../../services/auth/auth-service';

@Component({
    selector: 'app-identity-page',
    standalone: false,
    templateUrl: './identity-page.html',
    styleUrl: './identity-page.scss',
})
export class IdentityPage implements OnInit {

    protected readonly authService = inject(AuthService)
    private readonly identityService = inject(IdentityService)
    protected readonly identity = signal<IdentityResponse | null>(null)

    @Input() identityId!: string

    ngOnInit(): void {
        this.identityService.findById(this.identityId).subscribe({
            next: value => this.identity.set(value),
            error: _ => this.identity.set(null)
        })
    }

    protected readonly Authority = Authority;
}

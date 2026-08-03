import {Component, inject, OnInit, signal} from '@angular/core';
import {IdentityService} from '../../services/identity/identity-service';
import {IdentityResponse} from '../../models/all.model';

@Component({
    selector: 'app-identity-list',
    standalone: false,
    templateUrl: './identity-list.html',
    styleUrl: './identity-list.scss',
})
export class IdentityList implements OnInit {

    private readonly identityService = inject(IdentityService)
    identities = signal<IdentityResponse[]>([])

    ngOnInit(): void {
        this.identityService.findAll().subscribe({
            next: value => this.identities.set(value),
            error: err => console.log(err)
        })
    }
}

import {Component, Input} from '@angular/core';
import {IdentityResponse} from '../../models/all.model';

@Component({
    selector: 'app-identity-card',
    standalone: false,
    templateUrl: './identity-card.html',
    styleUrl: './identity-card.scss',
})
export class IdentityCard {

    @Input() identity!: IdentityResponse
}

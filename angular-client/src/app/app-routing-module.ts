import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Login} from './routes/login/login';
import {Registration} from './routes/registration/registration';
import {IdentityPage} from './routes/identity-page/identity-page';

const routes: Routes = [
    {path: 'login', component: Login},
    {path: 'registration', component: Registration},
    {path: 'identities/by-id', component: IdentityPage}
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {bindToComponentInputs: true})],
    exports: [RouterModule],
})
export class AppRoutingModule {}

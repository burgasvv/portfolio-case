import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Login} from './routes/login/login';
import {Registration} from './routes/registration/registration';

const routes: Routes = [
    {path: 'login', component: Login},
    {path: 'registration', component: Registration}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})
export class AppRoutingModule {}

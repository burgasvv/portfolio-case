import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { Header } from './components/header/header';
import { Footer } from './components/footer/footer';
import { Login } from './routes/login/login';
import { Registration } from './routes/registration/registration';
import {FormsModule} from '@angular/forms';
import {csrfInterceptor} from './interceptor/csrf.interceptor';

@NgModule({
    declarations: [App, Header, Footer, Login, Registration],
    imports: [BrowserModule, AppRoutingModule, FormsModule],
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideHttpClient(withInterceptors([csrfInterceptor]))
    ],
    bootstrap: [App],
})
export class AppModule {}

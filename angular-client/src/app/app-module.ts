import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { provideHttpClient } from '@angular/common/http';
import { Header } from './components/header/header';
import { Footer } from './components/footer/footer';
import { Login } from './routes/login/login';
import { Registration } from './routes/registration/registration';
import { FormsModule } from '@angular/forms';
import { IdentityPage } from './routes/identity-page/identity-page';
import { ImageThumbnail } from './components/image-thumbnail/image-thumbnail';
import { NgOptimizedImage } from '@angular/common';
import { ImagePage } from './components/image-page/image-page';
import { ImageCard } from './components/image-card/image-card';
import { IdentityCard } from './components/identity-card/identity-card';
import { IdentityList } from './routes/identity-list/identity-list';

@NgModule({
    declarations: [
        App,
        Header,
        Footer,
        Login,
        Registration,
        IdentityPage,
        ImageThumbnail,
        ImagePage,
        ImageCard,
        IdentityCard,
        IdentityList,
    ],
    imports: [BrowserModule, AppRoutingModule, FormsModule, NgOptimizedImage],
    providers: [provideBrowserGlobalErrorListeners(), provideHttpClient()],
    bootstrap: [App],
})
export class AppModule {}

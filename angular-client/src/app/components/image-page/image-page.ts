import {Component, effect, inject, input, signal} from '@angular/core';
import {ImageService} from '../../services/image/image-service';

@Component({
    selector: 'app-image-page',
    standalone: false,
    templateUrl: './image-page.html',
    styleUrl: './image-page.scss',
})
export class ImagePage {

    private readonly imageService = inject(ImageService)
    imageId = input<string, string | undefined>('', {
        transform: (value) => value ?? ''
    });
    imageUrl = signal<string | null>(null);

    constructor() {
        effect(() => {
            const id = this.imageId();
            if (!id) {
                this.cleanupUrl();
                return;
            }
            this.imageService.getImage(id)
                .subscribe({
                    next: (blob: Blob) => {
                        this.cleanupUrl();
                        this.imageUrl.set(URL.createObjectURL(blob));
                    },
                    error: (err) => {
                        console.error('Ошибка при загрузке изображения:', err);
                        this.cleanupUrl();
                    }
                });
        });
    }

    private cleanupUrl(): void {
        const currentUrl = this.imageUrl();
        if (currentUrl) {
            URL.revokeObjectURL(currentUrl);
        }
        this.imageUrl.set(null);
    }
}

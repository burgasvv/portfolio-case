import {ChangeDetectorRef, Component, inject, Input, OnInit} from '@angular/core';
import {ImageService} from '../../services/image/image-service';

@Component({
    selector: 'app-image-thumbnail',
    standalone: false,
    templateUrl: './image-thumbnail.html',
    styleUrl: './image-thumbnail.scss',
})
export class ImageThumbnail implements OnInit {

    private readonly imageService = inject(ImageService)
    @Input({transform: (value: string | undefined): string => value ?? ''}) imageId!: string
    imageUrl: string | null = null
    private readonly cdr = inject(ChangeDetectorRef);

    ngOnInit(): void {
        this.imageService.getImage(this.imageId).subscribe({
            next: (blob: Blob) => {
                if (this.imageUrl) URL.revokeObjectURL(this.imageUrl)
                this.imageUrl = URL.createObjectURL(blob)
                this.cdr.detectChanges()
            },
            error: (err) => {
                console.error('Ошибка при загрузке изображения:', err);
                this.imageUrl = null
            }
        });
    }
}

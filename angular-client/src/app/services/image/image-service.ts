import {inject, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Service()
export class ImageService {

    private readonly url: string = "http://localhost:9000/api/v1/images"
    private readonly http = inject(HttpClient)

    getImage(id: string): Observable<Blob> {
        return this.http.get(`${this.url}/by-id?imageId=${id}`, { responseType: 'blob', withCredentials: true })
    }
}

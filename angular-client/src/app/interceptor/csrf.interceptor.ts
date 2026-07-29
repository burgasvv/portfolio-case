import {HttpInterceptorFn} from '@angular/common/http';

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {

    const sensitiveMethods = ['POST', 'PUT', 'DELETE', 'PATCH'];

    if (sensitiveMethods.includes(req.method)) {
        const clonedReq = req.clone({
            headers: req.headers.set('X-CSRF-Token', crypto.randomUUID())
        });
        return next(clonedReq);
    }

    return next(req);
};

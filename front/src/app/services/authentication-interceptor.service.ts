import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {Injectable} from '@angular/core';

const unlockUrlRegexp = new RegExp('api/todos/[^/]+/unlock');

@Injectable()
export class AuthenticationInterceptorService implements HttpInterceptor {


  constructor(private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request)
      .pipe(
        tap(
          null,
          (err: any) => {
            if (err instanceof HttpErrorResponse) {
              // 401 errors forward to login page except for unlock.
              if (err.status === 401 && !unlockUrlRegexp.test(request.url)) {
                // redirect to the login route
                // or show a modal
                this.router.navigate(['login']);
              }
            }
          }
        )
      );
  }
}

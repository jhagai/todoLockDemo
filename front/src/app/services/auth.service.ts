import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Claims} from '../models/claims.model';
import {tap} from 'rxjs/operators';
import {Observable, ReplaySubject} from 'rxjs';

@Injectable({providedIn: 'root'})
export class AuthService {

  public static readonly AUTH_CLAIMS_URL = 'api/auth/claims';

  private _claims: Claims;

  private _claims$ = new ReplaySubject<Claims>(1);

  get claims(): Claims {
    return this._claims;
  }

  get claims$(): Observable<Claims> {
    return this._claims$.asObservable();
  }

  constructor(private http: HttpClient) {
  }

  authenticate(login: string, password: string) {
    return this.http.post<Claims>('api/auth/login', {login, password}, {withCredentials: true})
      .pipe(
        tap(claims => {
          this._claims = claims;
          this._claims$.next(claims);
        })
      );
  }

  logout() {
    return this.http.post<Claims>('api/auth/logout', null, {withCredentials: true})
      .pipe(
        tap(() => {
          this._claims = null;
          this._claims$.next(null);
        })
      );
  }

  getClaims() {
    return this.http.get<Claims>(AuthService.AUTH_CLAIMS_URL, {withCredentials: true})
      .pipe(
        tap(claims => {
          this._claims = claims;
          this._claims$.next(claims);
        })
      );
  }
}

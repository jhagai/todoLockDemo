import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from './services/auth.service';
import {of, Subject} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {exhaustMap, finalize, mergeMap, tap} from 'rxjs/operators';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  private logoutSubject = new Subject<void>();

  public loggingOut = false;

  constructor(public authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    this.logoutSubject
      .pipe(
        exhaustMap(
          () => {
            return of(true).pipe(
              tap(() => this.loggingOut = true),
              mergeMap(() => this.authService.logout()),
              tap(() => this.router.navigate(['login'])),
              finalize(() => this.loggingOut = false)
            );
          }
        ),
        untilDestroyed(this)
      )
      .subscribe();
  }

  logout() {
    this.logoutSubject.next();
  }

  ngOnDestroy(): void {
  }
}

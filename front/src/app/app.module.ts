import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {LoginComponent} from './login/login.component';
import {RouterModule} from '@angular/router';
import {appRoutes} from './routes/app.routes';
import {ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {DashboardComponent} from './dashboard/dashboard.component';
import {TodoFormComponent} from './todo-form/todo-form.component';
import {DisplayTodoComponent} from './display-todo/display-todo.component';
import {CreateTodoComponent} from './create-todo/create-todo.component';
import {EditTodoComponent} from './edit-todo/edit-todo.component';
import {AuthService} from './services/auth.service';
import {AuthenticationInterceptorService} from './services/authentication-interceptor.service';
import {catchError} from 'rxjs/operators';
import {EMPTY} from 'rxjs';

function init_app(authService: AuthService) {
  return () => authService.getClaims().pipe(catchError(() => EMPTY)).toPromise();
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DashboardComponent,
    TodoFormComponent,
    DisplayTodoComponent,
    CreateTodoComponent,
    EditTodoComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule.forRoot(appRoutes)
  ],
  providers: [
    {provide: APP_INITIALIZER, useFactory: init_app, deps: [AuthService], multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptorService, multi: true},
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

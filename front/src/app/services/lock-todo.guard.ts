import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate} from '@angular/router';
import {Observable, of} from 'rxjs';
import {TodoService} from './todo.service';
import {catchError, mapTo, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class LockTodoGuard implements CanDeactivate<any> {

  constructor(private todoService: TodoService) {
  }

  canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot): Observable<boolean> {
    const todoId = currentRoute.params['todo'];
    return this.todoService.unlockTodo(todoId)
      .pipe(
        tap(
          null,
          error => {
            console.log(error);
          }
        )
      )
      .pipe(catchError(() => of(true)), mapTo(true));

  }
}

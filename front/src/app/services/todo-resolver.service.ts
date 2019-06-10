import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Todo} from '../models/todo.model';
import {TodoService} from './todo.service';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TodoResolverService implements Resolve<Todo> {

  constructor(private todoService: TodoService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Todo> | Promise<Todo> | Todo {
    const todoId = route.params['todo'];
    return this.todoService.lockTodo(todoId).pipe(
      tap(
        null,
        error => {
          if (error.status === 423) {
            console.log(error);
            window.alert(`Todo is already locked by ${error.error.user.login}`);
          } else {
            window.alert('Cannot go to edit page.');
          }
        }
      )
    );
  }
}

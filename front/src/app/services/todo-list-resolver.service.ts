import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {TodoService} from './todo.service';
import {Todo} from '../models/todo.model';

@Injectable({providedIn: 'root'})
export class TodoListResolverService implements Resolve<Todo[]> {

  constructor(private todoService: TodoService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Todo[]> {
    return this.todoService.getTodoList();
  }

}

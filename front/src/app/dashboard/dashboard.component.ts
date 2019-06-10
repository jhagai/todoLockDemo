import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {Claims} from '../models/claims.model';
import {AuthService} from '../services/auth.service';
import {TodoService} from '../services/todo.service';
import {Todo} from '../models/todo.model';
import {TodoLock} from '../models/todo-lock.model';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  claims: Claims;
  todos$: Observable<Todo[]>;

  constructor(private route: ActivatedRoute, private authService: AuthService, private todoService: TodoService) {
  }

  ngOnInit() {
    this.claims = this.authService.claims;
    this.todos$ = this.route.data.pipe(
      map(({todos}: { todos: Todo[] }) => todos)
    );
  }

  isLocked(todoLock: TodoLock) {
    return todoLock && todoLock.user.userId !== this.claims.userId && todoLock.endDate > (new Date()).getTime() / 1000;
  }

  deleteTodo(todo: Todo) {
    this.todoService.deleteTodo(todo.id + '', todo.version)
      .subscribe(
        () => {
          window.alert(`${todo.title} removed successfully.`);
        },
        (httpError: HttpErrorResponse) => {
          if (httpError.status === 409) {
            window.alert(`${todo.title} has been modified.`);
          } else if (httpError.status === 404) {
            window.alert(`${todo.title} doesn't exist anymore.`);
          } else if (httpError.status === 423) {
            window.alert(`${todo.title} is locked by ${httpError.error.todoLock.user.login}.`);
          } else {
            window.alert(`An error occurred while trying to delete ${todo.title}.`);
          }
        }
      );
  }

}

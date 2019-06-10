import {Component, OnDestroy, OnInit} from '@angular/core';
import {fromEvent, Observable, of, Subject, timer} from 'rxjs';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TodoService} from '../services/todo.service';
import {map, mergeMap, takeUntil, tap} from 'rxjs/operators';
import {Todo} from '../models/todo.model';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 'app-edit-todo',
  templateUrl: './edit-todo.component.html',
  styleUrls: ['./edit-todo.component.css']
})
export class EditTodoComponent implements OnInit, OnDestroy {

  formGroup$: Observable<FormGroup>;

  endDate: number;

  // Timer to show the estimation of the lock's remaining duration (only for demo purpose).
  readonly timeoutDelay$ = timer(0, 1000)
    .pipe(
      map(
        () => {
          const endDateMinusDelay = (this.endDate) * 1000;
          const currentTime = new Date().getTime();
          return (endDateMinusDelay - currentTime) / 1000;
        }
      )
    );

  constructor(private fb: FormBuilder, private route: ActivatedRoute, private todoService: TodoService, private router: Router) {
    const formGroup = this.fb.group({
        id: this.fb.control(null),
        version: this.fb.control(null),
        title: this.fb.control(''),
        text: this.fb.control('')
      }
    );
    this.formGroup$ = of(formGroup);
  }

  ngOnInit() {

    const destroy = new Subject();

    this.formGroup$ = this.route.data.pipe(
      tap(
        ({todo}: { todo: Todo }) => {
          // Receive data from resolver

          // Setup a listener to trigger a todo unlock when user closes the browser tab.
          destroy.next();
          fromEvent(window, 'beforeunload')
            .pipe(
              mergeMap(
                () => {
                  return this.todoService
                    .unlockTodo(todo.id);
                }
              ),
              takeUntil(destroy),
              untilDestroyed(this))
            .subscribe();

          // Infinite recursive function that triggers the lock refresh endpoint.
          this.refreshLock(todo.todoLock.endDate, todo.id);
        }
      ),
      map(({todo}: { todo: Todo }) =>
        this.fb.group({
            id: this.fb.control(todo.id),
            version: this.fb.control(todo.version),
            title: this.fb.control(todo.title),
            text: this.fb.control(todo.text)
          }
        )
      )
    );
  }

  private refreshLock(endDate: number, todoId: number) {
    this.endDate = endDate;

    const endDateMinusDelay = (endDate - 30) * 1000;
    const currentTime = new Date().getTime();

    let timeoutDelay = endDateMinusDelay - currentTime;

    if (timeoutDelay < 0) {
      timeoutDelay = 0;
    }

    return setTimeout(
      () => {
        this.todoService.refreshlockTodo(todoId)
          .pipe(
            tap(
              lockInfo => this.refreshLock(lockInfo.endDate, todoId)
            ),
            untilDestroyed(this)
          )
          .subscribe();
      },
      timeoutDelay
    );
  }

  submit(data: Todo) {
    this.todoService.updateTodo(data).subscribe
    (() => {
        this.router.navigate(['display', data.id]);
      }
    );
  }

  ngOnDestroy(): void {
  }
}

import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Todo} from '../models/todo.model';
import {ActivatedRoute} from '@angular/router';
import {map} from 'rxjs/operators';

@Component({
  selector: 'app-display-todo',
  templateUrl: './display-todo.component.html',
  styleUrls: ['./display-todo.component.css']
})
export class DisplayTodoComponent implements OnInit {

  todo$: Observable<Todo>;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.todo$ = this.route.data.pipe(
      map(({todo}: { todo: Todo }) => todo)
    );
  }

}

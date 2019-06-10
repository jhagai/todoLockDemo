import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormGroup} from '@angular/forms';

@Component({
  selector: 'app-todo-form',
  templateUrl: './todo-form.component.html',
  styleUrls: ['./todo-form.component.css']
})
export class TodoFormComponent {

  @Input()
  formGroup: FormGroup;

  @Output()
  validate = new EventEmitter<{ id: number, title: string, text: string }>();

  constructor() {
  }
}

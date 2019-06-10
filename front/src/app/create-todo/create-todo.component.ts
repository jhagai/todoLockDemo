import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {TodoService} from '../services/todo.service';

@Component({
  selector: 'app-create-todo',
  templateUrl: './create-todo.component.html',
  styleUrls: ['./create-todo.component.css']
})
export class CreateTodoComponent implements OnInit {

  formGroup: FormGroup;

  constructor(private fb: FormBuilder, private todoService: TodoService, private router: Router) {
    this.formGroup = this.fb.group({
        title: this.fb.control(''),
        text: this.fb.control('')
      }
    );
  }

  ngOnInit() {
  }

  submit(data: { title: string, text: string }) {
    this.todoService.createTodo(data).subscribe
    ((newTodoId: number) => {
        this.router.navigate(['display', newTodoId]);
      }
    );
  }
}

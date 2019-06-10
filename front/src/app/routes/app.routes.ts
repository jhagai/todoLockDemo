import {Routes} from '@angular/router';
import {LoginComponent} from '../login/login.component';
import {DashboardComponent} from '../dashboard/dashboard.component';
import {TodoListResolverService} from '../services/todo-list-resolver.service';
import {TodoResolverService} from '../services/todo-resolver.service';
import {DisplayTodoComponent} from '../display-todo/display-todo.component';
import {LockTodoGuard} from '../services/lock-todo.guard';
import {EditTodoComponent} from '../edit-todo/edit-todo.component';
import {CreateTodoComponent} from '../create-todo/create-todo.component';

export const appRoutes: Routes = [
  {path: 'login', component: LoginComponent},
  {path: 'dashboard', component: DashboardComponent, resolve: {todos: TodoListResolverService}},
  {path: 'create', component: CreateTodoComponent},
  {path: 'display/:todo', component: DisplayTodoComponent, resolve: {todo: TodoResolverService}},
  {
    path: 'edit/:todo', component: EditTodoComponent,
    resolve: {todo: TodoResolverService}, canDeactivate: [LockTodoGuard]
  },
  {path: '**', component: LoginComponent}
];

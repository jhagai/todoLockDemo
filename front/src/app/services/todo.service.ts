import {Injectable} from '@angular/core';
import {Todo} from '../models/todo.model';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TodoLock} from '../models/todo-lock.model';

export interface LockInfo {
  userId: number;
  username: string;
  endDate: number;
}


@Injectable({providedIn: 'root'})
export class TodoService {

  public static readonly TODOS_URL = 'api/todos';
  public static readonly TODO_URL = todo => `${TodoService.TODOS_URL}/${todo}`;
  public static readonly TODO_VERSION_URL = (todo, version) => `${TodoService.TODOS_URL}/${todo}/${version}`;
  public static readonly LOCK_TODO_URL = todo => `${TodoService.TODOS_URL}/${todo}/lock`;
  public static readonly REFRESH_LOCK_TODO_URL = todo => `${TodoService.TODOS_URL}/${todo}/refreshLock`;
  public static readonly UNLOCK_TODO_URL = todo => `${TodoService.TODOS_URL}/${todo}/unlock`;
  public static readonly LOCK_TODO_STREAM_URL = todo => `${TodoService.TODOS_URL}/${todo}/lock/stream`;

  constructor(private http: HttpClient) {
  }

  getTodo(todoId: number) {
    return this.http.get<Todo>(TodoService.TODO_URL(todoId), {withCredentials: true});
  }

  getTodoList(): Observable<Todo[]> {
    return this.http.get<Todo[]>(TodoService.TODOS_URL, {withCredentials: true});
  }

  createTodo(data: { title: string, text: string }) {
    return this.http.post(TodoService.TODOS_URL, data, {withCredentials: true});
  }

  deleteTodo(todoId: string, version: number) {
    return this.http.delete(TodoService.TODO_VERSION_URL(todoId, version), {withCredentials: true});
  }

  updateTodo(data: Todo) {
    return this.http.put(TodoService.TODO_URL(data.id), data, {withCredentials: true});
  }

  unlockTodo(todoId: string | number) {
    return this.http.post(TodoService.UNLOCK_TODO_URL(todoId), null, {withCredentials: true});
  }

  lockTodo(todoId: string) {
    return this.http.post<Todo>(TodoService.LOCK_TODO_URL(todoId), null, {withCredentials: true});
  }

  refreshlockTodo(todoId: string | number): Observable<TodoLock> {
    return this.http.post<TodoLock>(TodoService.REFRESH_LOCK_TODO_URL(todoId), null, {withCredentials: true});
  }
}

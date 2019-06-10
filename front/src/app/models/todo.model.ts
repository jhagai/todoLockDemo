import {TodoLock} from './todo-lock.model';

export interface Todo {
  id: number;
  title: string;
  text: string;
  version: number;
  todoLock: TodoLock;
}

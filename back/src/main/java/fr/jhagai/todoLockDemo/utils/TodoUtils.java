package fr.jhagai.todoLockDemo.utils;

import fr.jhagai.todoLockDemo.dto.TodoDto;
import fr.jhagai.todoLockDemo.dto.TodoLockDto;
import fr.jhagai.todoLockDemo.dto.UserDto;
import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.entities.TodoLock;
import fr.jhagai.todoLockDemo.entities.User;

public class TodoUtils {

    public static TodoDto mapToDto(Todo todo) {
        final TodoDto todoDto = new TodoDto();
        todoDto.setId(todo.getId());
        todoDto.setTitle(todo.getTitle());
        todoDto.setText(todo.getText());
        todoDto.setVersion(todo.getVersion());

        final TodoLock todoLock = todo.getTodoLock();
        final TodoLockDto todoLockDto = todoLock != null ? mapToDto(todoLock) : null;
        todoDto.setTodoLock(todoLockDto);

        return todoDto;
    }

    public static TodoLockDto mapToDto(TodoLock todoLock) {

        final TodoLockDto todoLockDto = new TodoLockDto();
        todoLockDto.setId(todoLock.getId());
        todoLockDto.setVersion(todoLock.getVersion());
        todoLockDto.setEndDate(todoLock.getEndDate());
        todoLockDto.setCount(todoLock.getCount());
        final User user = todoLock.getUser();
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getId());
        userDto.setLogin(user.getLogin());
        todoLockDto.setUser(userDto);

        return todoLockDto;
    }
}

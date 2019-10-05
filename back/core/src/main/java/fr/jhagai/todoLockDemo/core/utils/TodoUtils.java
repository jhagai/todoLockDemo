package fr.jhagai.todoLockDemo.core.utils;

import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.dto.TodoLockDto;
import fr.jhagai.todoLockDemo.core.dto.UserDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.TodoLock;
import fr.jhagai.todoLockDemo.core.entities.User;

public class TodoUtils {

    public static final long LOCK_DURATION = 3600;

    public static TodoDto mapToDto(Todo todo) {
        final TodoDto todoDto = new TodoDto();
        todoDto.setId(todo.getId());
        todoDto.setTitle(todo.getTitle());
        todoDto.setText(todo.getText());
        todoDto.setVersion(todo.getVersion());

        if (todo.isLocked()) {
            final TodoLock todoLock = todo.getTodoLock();
            final TodoLockDto todoLockDto = mapToDto(todoLock);
            todoDto.setTodoLock(todoLockDto);
        }

        return todoDto;
    }

    public static TodoLockDto mapToDto(TodoLock todoLock) {

        final TodoLockDto todoLockDto = new TodoLockDto();
        todoLockDto.setEndDate(todoLock.getEndDate());
        final User user = todoLock.getUser();

        UserDto userDto = new UserDto();
        userDto.setUserId(user.getId());
        userDto.setLogin(user.getLogin());
        todoLockDto.setUser(userDto);

        return todoLockDto;
    }
}

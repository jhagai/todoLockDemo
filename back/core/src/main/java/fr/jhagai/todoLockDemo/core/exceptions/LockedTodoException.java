package fr.jhagai.todoLockDemo.core.exceptions;

import fr.jhagai.todoLockDemo.core.dto.TodoDto;

public class LockedTodoException extends TodoException {

    private final TodoDto todo;

    public LockedTodoException() {
        super();
        this.todo = null;
    }

    public LockedTodoException(final TodoDto todo) {
        super();
        this.todo = todo;
    }

    public TodoDto getTodo() {
        return todo;
    }
}

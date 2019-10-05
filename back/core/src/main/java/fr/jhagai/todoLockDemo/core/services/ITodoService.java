package fr.jhagai.todoLockDemo.core.services;

import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotLockedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ITodoService {
    List<TodoDto> getAllTodos();

    @Transactional(readOnly = true)
    Optional<TodoDto> getTodo(Long id);

    @Transactional
    Long addTodo(String label, String text);

    @Transactional
    void tryToDeleteTodo(Long userId, Long todoId, Long todoVersion) throws TodoNotFoundException, LockedTodoException, StaleTodoException;

    @Transactional
    Todo tryToUpdate(Long userId, TodoDto todo) throws StaleTodoException, LockedTodoException, TodoNotFoundException;

    @Transactional
    Long lock(Long userId, Long todoId) throws LockedTodoException, TodoNotFoundException, StaleTodoException;

    @Transactional
    TodoDto refreshLock(Long userId, Long todoId, Long todoLockToken) throws LockedTodoException, TodoNotFoundException, TodoNotLockedException;

    @Transactional
    void unlock(Long userId, Long todoId, Long todoLockToken) throws TodoNotFoundException, LockedTodoException, StaleTodoException, TodoNotLockedException;
}

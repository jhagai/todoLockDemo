package fr.jhagai.todoLockDemo.services;

import fr.jhagai.todoLockDemo.dto.TodoDto;
import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotLockedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ITodoService {
    List<Todo> getAllTodos();

    @Transactional(readOnly = true)
    Optional<Todo> getTodo(Long id);

    @Transactional
    Long addTodo(String label, String text);

    @Transactional
    void tryToDeleteTodo(Long userId, Long todoId, Long todoVersion) throws TodoNotFoundException, LockedTodoException, StaleTodoException;

    @Transactional
    Todo tryToUpdate(Long userId, TodoDto todo) throws StaleTodoException, LockedTodoException, TodoNotFoundException;

    @Transactional
    TodoDto lock(Long userId, Long todoId) throws LockedTodoException, TodoNotFoundException, StaleTodoException;

    @Transactional
    TodoDto refreshLock(Long userId, Long todoId) throws LockedTodoException, TodoNotFoundException, TodoNotLockedException;

    @Transactional
    void unlock(Long userId, Long todoId) throws TodoNotFoundException, LockedTodoException, StaleTodoException, TodoNotLockedException;
}

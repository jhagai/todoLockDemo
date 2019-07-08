package fr.jhagai.todoLockDemo.services;

import fr.jhagai.todoLockDemo.dao.TodoLockRepository;
import fr.jhagai.todoLockDemo.dao.TodoRepository;
import fr.jhagai.todoLockDemo.dao.UserRepository;
import fr.jhagai.todoLockDemo.dto.TodoDto;
import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.entities.TodoLock;
import fr.jhagai.todoLockDemo.entities.User;
import fr.jhagai.todoLockDemo.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotLockedException;
import fr.jhagai.todoLockDemo.utils.TodoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static fr.jhagai.todoLockDemo.TodoController.LOCK_DURATION;


@Service
public class TodoService implements ITodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoLockRepository todoLockRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Todo> getAllTodos() {
        return this.todoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Todo> getTodo(final Long id) {
        return this.todoRepository.findById(id);
    }

    @Override
    @Transactional
    public Long addTodo(final String label, final String text) {
        final Todo todoEntity = addTodoDb(label, text);
        return todoEntity.getId();
    }

    private Todo addTodoDb(String label, String text) {
        final Todo todoEntity = new Todo();
        // final TodoLock todoLock = new TodoLock();
        //todoLock.setCount(0L);
        //todoEntity.setTodoLock(todoLock);
        todoEntity.setTitle(label);
        todoEntity.setText(text);
        return todoRepository.save(todoEntity);
    }

    @Override
    @Transactional
    public void tryToDeleteTodo(final Long userId, final Long todoId, Long todoVersion) throws TodoNotFoundException, LockedTodoException, StaleTodoException {
        Optional<Todo> optionalTodo = this.todoRepository.findById(todoId);

        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        if (!todoVersion.equals(todo.getVersion())) {
            throw new StaleTodoException();
        }

        final User user = userRepository.getOne(userId);

        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoIdForRead(todoId);

        if (optionalTodoLock.isPresent()) {
            final TodoLock todoLock = optionalTodoLock.get();
            if (user.equals(todoLock.getUser())) {
                this.todoRepository.delete(todo);
            } else {
                throw new LockedTodoException(TodoUtils.mapToDto(todo));
            }
        } else {
            this.todoRepository.delete(todo);
        }
    }

    private Todo updateTodo(final Todo todo, TodoDto todoDto) {
        todo.setTitle(todoDto.getTitle());
        todo.setText(todoDto.getText());
        return todoRepository.saveAndFlush(todo);
    }

    @Override
    @Transactional
    public Todo tryToUpdate(final Long userId, final TodoDto todoDto) throws StaleTodoException, LockedTodoException, TodoNotFoundException {

        final Long todoId = todoDto.getId();

        final User user = userRepository.getOne(userId);

        // Check if todo exists
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);
        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);
        if (!ObjectUtils.nullSafeEquals(todo.getVersion(), todoDto.getVersion())) {
            throw new StaleTodoException();
        }

        // Check if lock exists (and acquire read lock to make sure no one else edits it).
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoIdForRead(todoId);
        final TodoLock todoLock = optionalTodoLock.orElseThrow(TodoNotFoundException::new);

        final Todo updatedTodo;
        if (todoLock.hasLock(user)) {
            updatedTodo = updateTodo(todo, todoDto);
        } else {
            throw new LockedTodoException();
        }
        return updatedTodo;
    }

    public static boolean hasLock(final User user, final Todo todo) {
        return todo.isLocked() && user.equals(todo.getTodoLock().getUser());
    }

    private boolean isLockedBySomeoneElse(final User user, final Todo todo) {
        return todo.isLocked() && !user.equals(todo.getTodoLock().getUser());
    }

    @Override
    @Transactional
    public TodoDto lock(final Long userId, final Long todoId) throws LockedTodoException, TodoNotFoundException, StaleTodoException {

        final User user = userRepository.getOne(userId);

        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);
        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        if (isLockedBySomeoneElse(user, todo)) {
            throw new LockedTodoException(TodoUtils.mapToDto(todo));
        }

        final LocalDateTime localDateTime = calcNewTimeout();
        TodoLock todoLock = todo.getTodoLock();
        long count = 0;
        if (todoLock == null) {
            todoLock = new TodoLock();
        } else if (todoLock.hasLock(user)) {
            count = todoLock.getCount();
        } else {
            count = 0L;
        }
        todoLock.setTodo(todo);
        todoLock.setEndDate(localDateTime);
        todoLock.setUser(user);
        todoLock.setCount(count + 1);

        TodoLock lockedTodoLock = this.todoLockRepository.saveAndFlush(todoLock);
        return TodoUtils.mapToDto(lockedTodoLock.getTodo());
    }

    private static LocalDateTime calcNewTimeout() {
        return LocalDateTime.now().plusSeconds(LOCK_DURATION);
    }

    @Override
    @Transactional
    public TodoDto refreshLock(final Long userId, final Long todoId) throws TodoNotFoundException, TodoNotLockedException, LockedTodoException {

        final User user = userRepository.getOne(userId);
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);

        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        if (hasLock(user, todo)) {
            final LocalDateTime newEndDate = calcNewTimeout();
            final TodoLock todoLock = todo.getTodoLock();
            todoLock.setEndDate(newEndDate);

            TodoLock refreshedTodoLock = todoLockRepository.saveAndFlush(todoLock);

            return TodoUtils.mapToDto(refreshedTodoLock.getTodo());
        } else {
            throw new TodoNotLockedException();
        }
    }

    @Override
    @Transactional
    public void unlock(final Long userId, final Long todoId) throws TodoNotFoundException, LockedTodoException, TodoNotLockedException {
        final User user = this.userRepository.getOne(userId);
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);

        final Todo todo = optionalTodo
                .orElseThrow(() -> new TodoNotFoundException());

        if (!todo.hasLock(user)) {
            throw new TodoNotLockedException();
        } else {
            final TodoLock todoLock = todo.getTodoLock();

            final Long count = todoLock.getCount();
            if (count > 1) {
                todoLock.setCount(count - 1);
                this.todoLockRepository.save(todoLock);
            } else {
                this.todoLockRepository.delete(todoLock);
            }
        }
    }
}

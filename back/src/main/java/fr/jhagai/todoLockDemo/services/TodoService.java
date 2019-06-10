package fr.jhagai.todoLockDemo.services;

import fr.jhagai.todoLockDemo.LockInfo;
import fr.jhagai.todoLockDemo.dao.TodoLockRepository;
import fr.jhagai.todoLockDemo.dao.TodoRepository;
import fr.jhagai.todoLockDemo.dao.UserRepository;
import fr.jhagai.todoLockDemo.dto.TodoDto;
import fr.jhagai.todoLockDemo.dto.TodoLockDto;
import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.entities.TodoLock;
import fr.jhagai.todoLockDemo.entities.User;
import fr.jhagai.todoLockDemo.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.utils.TodoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static fr.jhagai.todoLockDemo.TodoController.LOCK_DURATION;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoLockRepository todoLockRepository;

    @Autowired
    private UserRepository userRepository;

    public static ConcurrentHashMap<Long, SseEmitter> sses = new ConcurrentHashMap<>();

    public List<Todo> getAllTodos() {
        return this.todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Todo> getTodo(final Long id) {
        return this.todoRepository.findById(id);
    }

    @Transactional
    public Long addTodo(final String label, final String text) {
        final Todo todoEntity = addTodoDb(label, text);
        return todoEntity.getId();
    }

    private Todo addTodoDb(String label, String text) {
        final Todo todoEntity = new Todo();
        todoEntity.setTitle(label);
        todoEntity.setText(text);
        return todoRepository.save(todoEntity);
    }

    @Transactional
    public void tryToDeleteTodo(final Long userId, final Long todoId, Long todoVersion) throws TodoNotFoundException, LockedTodoException, StaleTodoException {
        Optional<Todo> optionalTodo = this.todoRepository.findById(todoId);

        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        if (!todoVersion.equals(todo.getVersion())) {
            throw new StaleTodoException();
        }

        final TodoLock todoLock = todo.getTodoLock();

        final User user = userRepository.getOne(userId);
        if (todoLock == null || user.equals(todoLock.getUser())) {
            this.todoRepository.delete(todo);
        } else {
            throw new LockedTodoException(TodoUtils.mapToDto(todo));
        }
    }

    private Todo updateTodo(final TodoDto todo) {
        final Optional<Todo> todoEntity = todoRepository.findByIdAndVersion(todo.getId(), todo.getVersion());
        return todoEntity.map(t -> {
            t.setTitle(todo.getTitle());
            t.setText(todo.getText());
            return todoRepository.save(t);
        }).orElse(null);
    }

    @Transactional
    public Todo tryToUpdate(final Long userId, final TodoDto todo) throws StaleTodoException, LockedTodoException, TodoNotFoundException {
        final boolean hasLock = hasLock(userId, todo.getId());
        final Todo updatedTodo;
        if (hasLock) {
            updatedTodo = updateTodo(todo);
            if (updatedTodo == null) {
                throw new StaleTodoException();
            }
        } else {
            throw new LockedTodoException();
        }
        return updatedTodo;
    }

    private boolean hasLock(final Long userId, final Long todoId) throws TodoNotFoundException {
        final Optional<TodoLock> byTodoId = todoLockRepository.findByTodoId(todoId);
        final User user = userRepository.getOne(userId);
        return byTodoId
                .map(
                        todoLock -> user.equals(todoLock.getUser())
                                && todoLock.getEndDate() != null
                                && LocalDateTime.now().isBefore(todoLock.getEndDate())
                )
                .orElseThrow(
                        () -> new TodoNotFoundException()
                );
    }

    @Transactional
    public TodoDto lock(final Long userId, final Long todoId) throws LockedTodoException {

        final Todo todo = todoRepository.getOne(todoId);
        // @TODO add version filtering
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoId(todoId);
        final User user = userRepository.getOne(userId);
        final TodoLock afterLock = optionalTodoLock
                .map(
                        todoLock -> {
                            // Check if existing lock is inactive or already locked by user
                            if (user.equals(todoLock.getUser()) || LocalDateTime.now().isAfter(todoLock.getEndDate())) {
                                todoLock.setUser(user);
                                todoLock.setCount(todoLock.getCount() + 1);
                                todoLock.setEndDate(LocalDateTime.now().plusSeconds(LOCK_DURATION));
                                return todoLock;
                            } else {
                                // Already locked by another user.
                                return todoLock;
                            }
                        }
                ).orElseGet(
                        () -> {
                            // Create new lock.
                            final TodoLock todoLock = new TodoLock();
                            todoLock.setCount(1l);
                            todoLock.setUser(user);
                            todoLock.setEndDate(LocalDateTime.now().plusSeconds(LOCK_DURATION));
                            todoLock.setTodo(todo);
                            return todoLock;
                        }
                );

        if (user.equals(afterLock.getUser())) {
            final TodoLock saved = this.todoLockRepository.save(afterLock);
            todo.setTodoLock(saved);
            return TodoUtils.mapToDto(this.todoRepository.save(todo));
        } else {
            throw new LockedTodoException(TodoUtils.mapToDto(afterLock.getTodo()));
        }
    }

    @Transactional
    public TodoLockDto refreshLock(final Long userId, final Long todoId) throws LockedTodoException {

        final User user = userRepository.getOne(userId);
        final Todo todo = todoRepository.getOne(todoId);
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoId(todoId);

        final TodoLock afterLock = optionalTodoLock
                .map(
                        todoLock -> {
                            // Check if existing lock is inactive or already locked by npm
                            if (user.equals(todoLock.getUser()) || LocalDateTime.now().isAfter(todoLock.getEndDate())) {
                                todoLock.setUser(user);
                                todoLock.setEndDate(LocalDateTime.now().plusSeconds(LOCK_DURATION));
                                return todoLock;
                            } else {
                                return todoLock;
                            }
                        }
                ).orElseGet(
                        () -> {
                            // Create new lock.
                            final TodoLock todoLock = new TodoLock();
                            todoLock.setCount(1l);
                            todoLock.setEndDate(LocalDateTime.now().plusSeconds(LOCK_DURATION));
                            todoLock.setTodo(todo);
                            return todoLock;
                        }
                );


        if (user.equals(afterLock.getUser())) {
            final TodoLock saved = this.todoLockRepository.save(afterLock);
            return TodoUtils.mapToDto(saved);
        } else {
            throw new LockedTodoException();
        }

    }

    @Transactional
    public void unlock(final Long userId, final Long todoId) throws TodoNotFoundException, LockedTodoException {
        final User user = this.userRepository.getOne(userId);
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoId(todoId);

        final TodoLock todoLock = optionalTodoLock
                .orElseThrow(() -> new TodoNotFoundException());

        if (user.equals(todoLock.getUser())) {
            if (todoLock.getCount() > 1) {
                todoLock.setCount(todoLock.getCount() - 1);
                this.todoLockRepository.save(todoLock);
            } else {
                this.todoLockRepository.delete(todoLock);
            }
        } else {
            throw new LockedTodoException();
        }
    }
}

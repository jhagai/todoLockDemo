package fr.jhagai.todoLockDemo.services;

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
        final TodoLock todoLock = new TodoLock();
        todoLock.setCount(0L);
        todoEntity.setTodoLock(todoLock);
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

        final TodoLock todoLock = todo.getTodoLock();

        final User user = userRepository.getOne(userId);
        if (todoLock == null || user.equals(todoLock.getUser())) {
            this.todoRepository.delete(todo);
        } else {
            throw new LockedTodoException(TodoUtils.mapToDto(todo));
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
        final Optional<Todo> optionalTodo = todoRepository.findByIdForWrite(todoId);

        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        if (!ObjectUtils.nullSafeEquals(todo.getVersion(), todoDto.getVersion())) {
            throw new StaleTodoException();
        }

        final Todo updatedTodo;
        if (hasLock(user, todo)) {
            updatedTodo = updateTodo(todo, todoDto);
        } else {
            throw new LockedTodoException();
        }
        return updatedTodo;
    }

    public static boolean hasLock(final User user, final Todo todo) {
        return user.equals(todo.getTodoLock().getUser())
                && todo.isLocked();
    }

    private boolean isLockedBySomeoneElse(final User user, final Todo todo) {
        return !user.equals(todo.getTodoLock().getUser())
                && todo.isLocked();
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
        int newLockCount = this.todoRepository.lock(todoId, localDateTime, user);
        if (newLockCount < 1) {
            int addlockCount = this.todoRepository.addlock(todoId, localDateTime, user);
            if (addlockCount < 1) {
                throw new StaleTodoException();
            }
        }
        final Todo saved = this.todoRepository.getOne(todoId);
        return TodoUtils.mapToDto(saved);
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

            // Refresh lock using jpql to bypass the version update.
            int count = this.todoRepository.refreshLock(todoId, newEndDate);
            if (count > 0) {

                final Optional<Todo> optionalTodo2 = todoRepository.findById(todoId);
                final Todo todo2 = optionalTodo2.orElseThrow(TodoNotFoundException::new);

                // Re check if todo is owned by user.
                if (!hasLock(user, todo2)) {
                    throw new LockedTodoException(TodoUtils.mapToDto(todo2));
                }

                return TodoUtils.mapToDto(todo2);
            } else {
                throw new TodoNotFoundException();
            }
        } else {
            throw new TodoNotLockedException();
        }
    }

    @Override
    @Transactional
    public void unlock(final Long userId, final Long todoId) throws TodoNotFoundException, LockedTodoException, StaleTodoException {
        final User user = this.userRepository.getOne(userId);
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);

        final Todo todo = optionalTodo
                .orElseThrow(() -> new TodoNotFoundException());

        if (isLockedBySomeoneElse(user, todo)) {
            // Locked by someone else.
            throw new LockedTodoException();
        } else {
            // Try to unlock.
            final int unlockCount = this.todoRepository.unlock(todoId, user);
            if (unlockCount < 1) {
                // Failed to unlock => means the lock is not owned by the user anymore.
                throw new StaleTodoException();
            }
        }
    }
}

package fr.jhagai.todoLockDemo.core.services;

import fr.jhagai.todoLockDemo.core.dao.TodoLockRepository;
import fr.jhagai.todoLockDemo.core.dao.TodoRepository;
import fr.jhagai.todoLockDemo.core.dao.UserRepository;
import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.TodoLock;
import fr.jhagai.todoLockDemo.core.entities.TodoLockToken;
import fr.jhagai.todoLockDemo.core.entities.User;
import fr.jhagai.todoLockDemo.core.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotLockedException;
import fr.jhagai.todoLockDemo.core.utils.TodoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.jhagai.todoLockDemo.core.utils.TodoUtils.LOCK_DURATION;

@Service
public class TodoService implements ITodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoLockRepository todoLockRepository;

    public void setTodoLockRepository(final TodoLockRepository todoLockRepository) {
        this.todoLockRepository = todoLockRepository;
    }

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TodoDto> getAllTodos() {
        return this.todoRepository.findAll().stream().map(TodoUtils::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TodoDto> getTodo(final Long id) {
        return this.todoRepository.findById(id).map(TodoUtils::mapToDto);
    }

    @Override
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

    @Override
    @Transactional
    public void tryToDeleteTodo(final Long userId, final Long todoId, Long todoVersion) throws TodoNotFoundException, LockedTodoException, StaleTodoException {
        Optional<Todo> optionalTodo = this.todoRepository.findById(todoId);

        // Check if todo exists
        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        // Check todo version
        if (!ObjectUtils.nullSafeEquals(todo.getVersion(), todoVersion)) {
            throw new StaleTodoException();
        }

        final User user = userRepository.getOne(userId);

        // Add read Lock to prevent any modification while deleting the todo.
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoIdForRead(todoId);
        // final Optional<TodoLock> optionalTodoLock = todoLockRepository.findById(todoId);

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

        // Check if todo has proper version
        if (!ObjectUtils.nullSafeEquals(todo.getVersion(), todoDto.getVersion())) {
            throw new StaleTodoException();
        }

        // Check if lock exists (and acquire read lock to make sure no one else edits it).
        final Optional<TodoLock> optionalTodoLock = todoLockRepository.findByTodoIdForRead(todoId);
        final TodoLock todoLock = optionalTodoLock.orElseThrow(TodoNotFoundException::new);

        final Todo updatedTodo;

        if (todoLock.hasLock(user)) {
            // If user owns lock => update todo
            updatedTodo = updateTodo(todo, todoDto);
        } else {
            // If user DOESN'T own lock => Exception
            throw new LockedTodoException();
        }
        return updatedTodo;
    }

    @Override
    @Transactional
    public Long lock(final Long userId, final Long todoId) throws LockedTodoException, TodoNotFoundException {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final User user = userRepository.getOne(userId);

        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);
        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        final Optional<TodoLock> optionalTodoLock = this.todoLockRepository.findByTodoIdForWrite(todoId);
        // final Optional<TodoLock> optionalTodoLock = this.todoLockRepository.findByTodoId(todoId);

        final TodoLock saved;

        final LocalDateTime localDateTime = calcNewTimeout();

        if (optionalTodoLock.isPresent()) {

            final TodoLock todoLock = optionalTodoLock.get();

            if (todoLock.hasLock(user)) {
                // User already owns the lock => add a new lock token.
                todoLock.setTodo(todo);
                todoLock.setEndDate(localDateTime);
                todoLock.setUser(user);
                final TodoLockToken token = new TodoLockToken();
                token.setTodoLock(todoLock);
                List<TodoLockToken> tokens = todoLock.getTokens();
                tokens.add(token);
                saved = this.todoLockRepository.saveAndFlush(todoLock);
            } else if (todoLock.isLockedBySomeoneElse(user)) {
                // Someone else already locks the todo => throw an exception.
                throw new LockedTodoException();
            } else {
                // Delete existing lock and replace it with a new one.
                this.todoLockRepository.delete(todoLock);
                final TodoLockToken todoLockToken = new TodoLockToken();
                todoLockToken.setTodoLock(todoLock);
                todoLock.getTokens().add(todoLockToken);
                todoLock.setTodo(todo);
                todoLock.setEndDate(localDateTime);
                todoLock.setUser(user);
                saved = this.todoLockRepository.saveAndFlush(todoLock);
            }

        } else {
            // No lock on the todo => create one
            // Concurrency is ensured by the OneToOne link (and the Constraint).
            final TodoLock newTodoLock = new TodoLock();
            final TodoLockToken todoLockToken = new TodoLockToken();
            todoLockToken.setTodoLock(newTodoLock);
            newTodoLock.setTokens(Arrays.asList(todoLockToken));
            newTodoLock.setTodo(todo);
            newTodoLock.setEndDate(localDateTime);
            newTodoLock.setUser(user);
            saved = this.todoLockRepository.saveAndFlush(newTodoLock);
        }

        // Pick last saved token.
        final TodoLockToken savedToken = saved.getTokens().stream().max(Comparator.comparingLong(TodoLockToken::getId)).get();

        return savedToken.getId();
    }

    private static LocalDateTime calcNewTimeout() {
        return LocalDateTime.now().plusSeconds(LOCK_DURATION);
    }

    @Override
    @Transactional
    public TodoDto refreshLock(final Long userId, final Long todoId, final Long todoLockToken) throws TodoNotFoundException, TodoNotLockedException {

        final User user = userRepository.getOne(userId);
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);

        final Todo todo = optionalTodo.orElseThrow(TodoNotFoundException::new);

        final Optional<TodoLock> optionalTodoLock = this.todoLockRepository.findByTodoIdForWrite(todoId);

        if (optionalTodoLock.isPresent() && todo.hasLock(user, todoLockToken)) {
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
    public void unlock(final Long userId, final Long todoId, final Long todoLockToken) throws TodoNotFoundException, LockedTodoException, TodoNotLockedException {
        final User user = this.userRepository.getOne(userId);
        final Optional<Todo> optionalTodo = todoRepository.findById(todoId);

        final Todo todo = optionalTodo
                .orElseThrow(() -> new TodoNotFoundException());

        final Optional<TodoLock> optionalTodoLock = this.todoLockRepository.findByTodoIdForWrite(todoId);

        if (optionalTodoLock.isPresent() && todo.hasLock(user, todoLockToken)) {
            final TodoLock todoLock = todo.getTodoLock();

            // Remove the lock token from list.
            todoLock.getTokens().removeIf(t -> todoLockToken.equals(t.getId()));

            if (todoLock.getTokens().size() > 0) {
                // There are locks left token => just remove the token.
                this.todoLockRepository.save(todoLock);
            } else {
                // Only one token => delete the whole lock.
                this.todoLockRepository.delete(todoLock);
            }
        } else {
            throw new TodoNotLockedException();
        }
    }
}

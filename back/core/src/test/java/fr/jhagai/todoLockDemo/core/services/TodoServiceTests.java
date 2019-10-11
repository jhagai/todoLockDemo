package fr.jhagai.todoLockDemo.core.services;

import fr.jhagai.todoLockDemo.core.dao.TodoRepository;
import fr.jhagai.todoLockDemo.core.dao.UserRepository;
import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.User;
import fr.jhagai.todoLockDemo.core.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.core.utils.ThrowingRunnable;
import fr.jhagai.todoLockDemo.core.utils.ThrowingSupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TodoServiceTests {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    Todo persistedTodo;

    Long cyrilId;

    Long arnaudId;

    @Before
    @Transactional
    public void before() {

        final Todo todo = new Todo();
        todo.setTitle("Original title");
        todo.setText("Original Text");
        this.persistedTodo = this.todoRepository.save(todo);

        final User cyril = new User();
        cyril.setLogin("cyril");
        cyril.setPassword("balit");


        this.cyrilId = this.userRepository.save(cyril).getId();

        final User arnaud = new User();
        arnaud.setLogin("arnaud");
        arnaud.setPassword("waller");

        this.arnaudId = this.userRepository.save(arnaud).getId();
    }

    @Test(expected = LockedTodoException.class)
    public void forbidConcurrentLocks() throws TodoNotFoundException, LockedTodoException {

        try {
            decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));
            decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));
        } catch (TodoException e) {
            Assert.fail("Test failed during preparation");
        }
        this.todoService.lock(arnaudId, this.persistedTodo.getId());
    }

    @Test
    public void canLockAfterReleased() throws TodoException {

        // GIVEN
        // Lock the todo with Cyril
        final Long lockToken = decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));

        final TodoDto todoDto = new TodoDto();
        todoDto.setId(this.persistedTodo.getId());
        todoDto.setTitle("new Title");
        todoDto.setText("new Text");
        todoDto.setVersion(this.persistedTodo.getVersion());
        // Update the todo
        decorateStep(() -> this.todoService.tryToUpdate(cyrilId, todoDto));

        // Release the lock
        decorateStep(() -> this.todoService.unlock(cyrilId, this.persistedTodo.getId(), lockToken));

        // WHEN
        // Lock the todo with Arnaud
        this.todoService.lock(arnaudId, this.persistedTodo.getId());

    }

    @Test(expected = StaleTodoException.class)
    public void optimisticLockError() throws TodoException {

        Long initialTodoVersion = 0L;
        try {
            // GIVEN
            // Lock the todo with Cyril
            final Long lockToken1 = decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));

            // Lock the todo with Cyril
            final Long lockToken2 = decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));

            final Optional<TodoDto> optionalInitialTodo = this.todoService.getTodo(this.persistedTodo.getId());
            final TodoDto initialTodoDto = optionalInitialTodo.get();

            initialTodoVersion = initialTodoDto.getVersion();

            final TodoDto todoDto1 = new TodoDto();
            todoDto1.setId(this.persistedTodo.getId());
            todoDto1.setTitle("new Title 1");
            todoDto1.setText("new Text 1");
            todoDto1.setVersion(initialTodoVersion);

            // Update the todo
            decorateStep(() -> this.todoService.tryToUpdate(cyrilId, todoDto1));


        } catch (Exception e) {
            Assert.fail("Test failed during preparation.");
        }

        final TodoDto todoDto2 = new TodoDto();
        todoDto2.setId(this.persistedTodo.getId());
        todoDto2.setTitle("new Title 2");
        todoDto2.setText("new Text 2");
        todoDto2.setVersion(initialTodoVersion);

        // WHEN
        // Update the todo (with and outdated version).
        decorateStep(() -> this.todoService.tryToUpdate(cyrilId, todoDto2));
    }

    private <E extends Exception> void decorateStep(ThrowingRunnable<E> r) throws E {
        r.run();
        this.entityManager.flush();
        this.entityManager.clear();
    }

    private <T, E extends Exception> T decorateStep(ThrowingSupplier<T, E> r) throws E {
        final T t = r.get();
        this.entityManager.flush();
        this.entityManager.clear();
        return t;
    }
}

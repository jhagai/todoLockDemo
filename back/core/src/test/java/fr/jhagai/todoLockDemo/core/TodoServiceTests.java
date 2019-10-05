package fr.jhagai.todoLockDemo.core;

import fr.jhagai.todoLockDemo.core.dao.TodoRepository;
import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.User;
import fr.jhagai.todoLockDemo.core.exceptions.*;
import fr.jhagai.todoLockDemo.core.services.TodoService;
import fr.jhagai.todoLockDemo.core.utils.ThrowingRunnable;
import fr.jhagai.todoLockDemo.core.utils.ThrowingSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TodoServiceTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoService todoService;

    Todo persistedTodo;

    Long cyrilId;

    Long arnaudId;

    @Before
    @Transactional
    public void before() {

        final Todo todo = new Todo();
        todo.setTitle("Original title");
        todo.setText("Original Text");
        this.persistedTodo = this.entityManager.persist(todo);

        final User cyril = new User();
        cyril.setLogin("cyril");
        cyril.setPassword("balit");


        this.cyrilId = this.entityManager.persist(cyril).getId();

        final User arnaud = new User();
        arnaud.setLogin("arnaud");
        arnaud.setPassword("waller");

        this.arnaudId = this.entityManager.persist(arnaud).getId();
    }

    @Test(expected = LockedTodoException.class)
    public void forbidConcurrentLocks() throws TodoException {

        decorateStep(() -> this.todoService.lock(cyrilId, this.persistedTodo.getId()));

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

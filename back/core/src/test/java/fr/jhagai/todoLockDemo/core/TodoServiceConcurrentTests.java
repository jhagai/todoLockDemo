package fr.jhagai.todoLockDemo.core;

import com.google.testing.threadtester.*;
import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.User;
import fr.jhagai.todoLockDemo.core.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoException;
import fr.jhagai.todoLockDemo.core.exceptions.TodoNotFoundException;
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

import java.util.HashSet;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TodoServiceConcurrentTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private volatile TodoService todoService;

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

    @Test
    public void forbidConcurrentLocks() throws TodoException {
        AnnotatedTestRunner runner = new AnnotatedTestRunner();
        HashSet<String> methods = new HashSet<>();
        runner.setMethodOption(MethodOption.ALL_METHODS, methods);
        runner.setDebug(true);
        runner.runTests(this.getClass(), TodoService.class);

    }

    @ThreadedMain
    public void main() throws LockedTodoException, StaleTodoException, TodoNotFoundException {
        this.todoService.lock(cyrilId, this.persistedTodo.getId());
    }

    // @ThreadedSecondary
    public void secondary() throws LockedTodoException, StaleTodoException, TodoNotFoundException {
        this.todoService.lock(arnaudId, this.persistedTodo.getId());
    }

    @ThreadedAfter
    public void after() {
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

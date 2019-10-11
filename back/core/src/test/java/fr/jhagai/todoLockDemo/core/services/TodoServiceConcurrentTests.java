package fr.jhagai.todoLockDemo.core.services;

import fr.jhagai.todoLockDemo.core.dao.TodoLockRepository;
import fr.jhagai.todoLockDemo.core.dao.TodoRepository;
import fr.jhagai.todoLockDemo.core.dao.UserRepository;
import fr.jhagai.todoLockDemo.core.entities.Todo;
import fr.jhagai.todoLockDemo.core.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "application.properties")
@ImportAutoConfiguration
@AutoConfigureTestDatabase
public class TodoServiceConcurrentTests {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    TodoLockRepository orginalTodoLockRepository;

    Long cyrilId;

    Long arnaudId;

    @Before
    @Transactional
    public void before() {

        if (cyrilId == null) {

            final User cyril = new User();
            cyril.setLogin("cyril");
            cyril.setPassword("balit");

            this.cyrilId = this.userRepository.save(cyril).getId();

            final User arnaud = new User();
            arnaud.setLogin("arnaud");
            arnaud.setPassword("waller");

            this.arnaudId = this.userRepository.save(arnaud).getId();
        }

    }


    @Transactional
    Todo createNewTodo() {
        final Todo todo = new Todo();
        todo.setTitle("Original title");
        todo.setText("Original Text");
        return this.todoRepository.save(todo);
    }

    @Test
    public void concurrentLock() throws ExecutionException, InterruptedException {

        final Todo persistedTodo = createNewTodo();

        CompletableFuture<Boolean> cyrilFutur = CompletableFuture.supplyAsync(() -> {
            boolean success = false;
            try {
                this.todoService.lock(cyrilId, persistedTodo.getId());
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        });

        CompletableFuture<Boolean> arnaudFutur = CompletableFuture.supplyAsync(() -> {
            boolean success = false;
            try {
                this.todoService.lock(arnaudId, persistedTodo.getId());
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        });

        Boolean cyrilResult = cyrilFutur.get();
        Boolean arnaudResult = arnaudFutur.get();

        Assert.assertTrue(arnaudResult ^ cyrilResult);
    }

    @Test
    public void other() {

    }
}

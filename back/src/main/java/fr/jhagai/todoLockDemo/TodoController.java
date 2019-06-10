package fr.jhagai.todoLockDemo;

import fr.jhagai.todoLockDemo.dto.CreateTodoDto;
import fr.jhagai.todoLockDemo.dto.TodoDto;
import fr.jhagai.todoLockDemo.dto.TodoLockDto;
import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.exceptions.LockedTodoException;
import fr.jhagai.todoLockDemo.exceptions.StaleTodoException;
import fr.jhagai.todoLockDemo.exceptions.TodoNotFoundException;
import fr.jhagai.todoLockDemo.security.TodoPrincipal;
import fr.jhagai.todoLockDemo.services.TodoService;
import fr.jhagai.todoLockDemo.utils.TodoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TodoController {

    public static final long LOCK_DURATION = 60;

    @Autowired
    private TodoService todoService;

    @GetMapping(path = "/todos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TodoDto>> listTodos() {
        return ResponseEntity.ok(
                todoService.getAllTodos().stream()
                        .map(TodoUtils::mapToDto)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping(path = "/todos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> createTodo(@RequestBody CreateTodoDto createTodoDto) {
        final Long todoId = todoService.addTodo(createTodoDto.getTitle(), createTodoDto.getText());
        return ResponseEntity.ok(todoId);
    }

    @DeleteMapping(path = "/todos/{todoId}/{todoVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTodo(final HttpServletRequest servletRequest,
                                        @PathVariable final Long todoId,
                                        @PathVariable final Long todoVersion) {
        final TodoPrincipal userPrincipal = getTodoPrincipal(servletRequest);
        final Long userId = userPrincipal.getUserId();
        try {
            todoService.tryToDeleteTodo(userId, todoId, todoVersion);
            return ResponseEntity.noContent().build();
        } catch (TodoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (LockedTodoException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getTodo());
        } catch (StaleTodoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping(path = "/todos/{todoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTodo(final HttpServletRequest servletRequest, @RequestBody TodoDto todo) {
        final TodoPrincipal userPrincipal = getTodoPrincipal(servletRequest);
        final Long userId = userPrincipal.getUserId();
        try {
            final Todo updatedTodo = todoService.tryToUpdate(userId, todo);
            return ResponseEntity.ok(TodoUtils.mapToDto(updatedTodo));
        } catch (StaleTodoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (LockedTodoException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        } catch (TodoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/todos/{todoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TodoDto> getTodo(@PathVariable final Long todoId) {
        Optional<TodoDto> foundTodo = todoService.getTodo(todoId).map(TodoUtils::mapToDto);
        if (foundTodo.isPresent()) {
            final TodoDto body = foundTodo.get();
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(path = "/todos/{todoId}/lock")
    public ResponseEntity<TodoDto> lock(
            final HttpServletRequest servletRequest,
            @PathVariable final Long todoId) {
        final TodoPrincipal userPrincipal = getTodoPrincipal(servletRequest);
        final Long userId = userPrincipal.getUserId();
        try {
            final TodoDto locked = todoService.lock(userId, todoId);
            return ResponseEntity.ok(locked);
        } catch (LockedTodoException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getTodo());
        }
    }

    public static TodoPrincipal getTodoPrincipal(HttpServletRequest servletRequest) {
        return (TodoPrincipal) ((UsernamePasswordAuthenticationToken) servletRequest.getUserPrincipal()).getPrincipal();
    }

    @PostMapping(path = "/todos/{todoId}/refreshLock")
    public ResponseEntity<?> refreshLock(final HttpServletRequest servletRequest,
                                         @PathVariable final Long todoId) {
        final TodoPrincipal userPrincipal = getTodoPrincipal(servletRequest);
        final Long userId = userPrincipal.getUserId();
        try {
            final TodoLockDto todoLockDto = todoService.refreshLock(userId, todoId);
            return ResponseEntity.ok(todoLockDto);
        } catch (LockedTodoException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
    }

    @PostMapping(path = "/todos/{todoId}/unlock")
    public ResponseEntity<?> unlock(final HttpServletRequest servletRequest,
                                    @PathVariable final Long todoId) {
        final TodoPrincipal userPrincipal = getTodoPrincipal(servletRequest);
        final Long userId = userPrincipal.getUserId();
        try {
            todoService.unlock(userId, todoId);
            return ResponseEntity.ok().build();
        } catch (LockedTodoException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        } catch (TodoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/stream/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> lockStream() {
        return Flux.interval(Duration.ofSeconds(10))
                .map(
                        sequence -> ServerSentEvent.<String>builder()
                                .id(sequence.toString())
                                .event("periodic-event")
                                .data("toto")
                                .build()
                )
                .doFinally(param -> System.out.println("Finally " + param));
    }
}

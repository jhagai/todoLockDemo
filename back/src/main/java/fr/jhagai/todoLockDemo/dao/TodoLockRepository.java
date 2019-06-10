package fr.jhagai.todoLockDemo.dao;

import fr.jhagai.todoLockDemo.entities.TodoLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoLockRepository extends JpaRepository<TodoLock, Long> {
    Optional<TodoLock> findByTodoId(Long todoId);
}

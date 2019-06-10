package fr.jhagai.todoLockDemo.dao;

import fr.jhagai.todoLockDemo.entities.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Optional<Todo> findByIdAndVersion(Long todoId, Long version);
}

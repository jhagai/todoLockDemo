package fr.jhagai.todoLockDemo.core.dao;

import fr.jhagai.todoLockDemo.core.entities.TodoLockToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoLockTokenRepository extends JpaRepository<TodoLockToken, Long> {
}

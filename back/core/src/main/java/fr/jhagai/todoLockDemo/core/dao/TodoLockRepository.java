package fr.jhagai.todoLockDemo.core.dao;

import fr.jhagai.todoLockDemo.core.entities.TodoLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TodoLockRepository extends JpaRepository<TodoLock, Long> {

    Optional<TodoLock> findByTodoId(@Param("todoId") Long todoId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select t from TodoLock t where t.todo.id = :todoId")
    Optional<TodoLock> findByTodoIdForRead(@Param("todoId") Long todoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TodoLock t where t.todo.id = :todoId")
    Optional<TodoLock> findByTodoIdForWrite(@Param("todoId") Long todoId);
}

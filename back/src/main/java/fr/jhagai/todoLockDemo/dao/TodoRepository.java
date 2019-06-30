package fr.jhagai.todoLockDemo.dao;

import fr.jhagai.todoLockDemo.entities.Todo;
import fr.jhagai.todoLockDemo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Todo t where t.id = :id")
    Optional<Todo> findByIdForWrite(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Todo t set t.todoLock.endDate = :endDate where t.id = :id")
    int refreshLock(@Param("id") Long id, @Param("endDate") LocalDateTime endDate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Todo t set t.todoLock.endDate = :endDate, t.todoLock.count = t.todoLock.count + 1 where t.id = :id and t.todoLock.user = :user and t.todoLock.count > 0 and t.todoLock.endDate is not null and t.todoLock.endDate > current_timestamp")
    int addlock(@Param("id") Long id, @Param("endDate") LocalDateTime endDate, @Param("user") User user);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Todo t set t.todoLock.endDate = :endDate, t.todoLock.user = :user, t.todoLock.count = 1 where t.id = :id and (t.todoLock.count < 1 or t.todoLock.endDate is null or t.todoLock.endDate < current_timestamp)")
    int lock(@Param("id") Long id, @Param("endDate") LocalDateTime endDate, @Param("user") User user);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Todo t set t.todoLock.count = t.todoLock.count - 1 where t.id = :id and t.todoLock.user = :user and t.todoLock.count > 0")
    int unlock(@Param("id") Long id, @Param("user") User user);
}

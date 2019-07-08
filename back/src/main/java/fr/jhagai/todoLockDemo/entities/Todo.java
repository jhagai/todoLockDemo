package fr.jhagai.todoLockDemo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "TODO")
public class Todo {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String text;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToOne(mappedBy = "todo", cascade = CascadeType.REMOVE)
    private TodoLock todoLock;

    public boolean isLocked() {
        return getTodoLock() != null &&
                getTodoLock().isActive();
    }

    public boolean hasLock(User user) {
        final TodoLock todoLock = getTodoLock();
        return todoLock != null && todoLock.hasLock(user);
    }
}

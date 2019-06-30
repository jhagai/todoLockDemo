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

    @Embedded
    private TodoLock todoLock;

    public boolean isLocked() {
        return getTodoLock().getCount() > 0
                && getTodoLock().getEndDate() != null
                && LocalDateTime.now().isBefore(getTodoLock().getEndDate());
    }
}

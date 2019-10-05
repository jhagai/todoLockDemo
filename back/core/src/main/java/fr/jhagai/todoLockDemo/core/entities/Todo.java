package fr.jhagai.todoLockDemo.core.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

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

    @OneToOne(mappedBy = "todo", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private TodoLock todoLock;

    public boolean isLocked() {
        return getTodoLock() != null &&
                getTodoLock().isActive();
    }

    public boolean hasLock(User user) {
        final TodoLock todoLock = getTodoLock();
        return todoLock != null && todoLock.hasLock(user);
    }

    public boolean hasLock(User user, Long lockToken) {
        return hasLock(user) && getTodoLock().getTokens().stream().anyMatch(t -> t.getId().equals(lockToken));
    }
}

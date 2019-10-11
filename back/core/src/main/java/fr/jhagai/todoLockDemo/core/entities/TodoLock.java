package fr.jhagai.todoLockDemo.core.entities;

import fr.jhagai.todoLockDemo.core.utils.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@Entity
@Table(
        name = "TodoLock", uniqueConstraints = {
        @UniqueConstraint(name = "todo", columnNames = {"TODO_ID"})}
)
public class TodoLock {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    private Long id;

    // @Version
    // private Long version;

    @Convert(converter = LocalDateTimeConverter.class)
    @Column()
    private LocalDateTime endDate;

    @OneToOne
    private Todo todo;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "todoLock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoLockToken> tokens = new ArrayList<>();

    public boolean isActive() {
        return this.getTokens().size() > 0 &&
                this.getEndDate() != null &&
                LocalDateTime.now().isBefore(this.getEndDate());
    }

    public boolean hasLock(final User user) {
        return user.equals(this.user) && this.isActive();
    }

    public boolean isLockedBySomeoneElse(final User user) {
        return !user.equals(this.user) && this.isActive();
    }
}

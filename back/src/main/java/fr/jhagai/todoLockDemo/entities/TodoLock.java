package fr.jhagai.todoLockDemo.entities;

import fr.jhagai.todoLockDemo.utils.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "TodoLock")
public class TodoLock {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Convert(converter = LocalDateTimeConverter.class)
    @Column()
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Long count;

    @OneToOne
    private Todo todo;

    @ManyToOne()
    private User user;

    public boolean isActive() {
        return this.count > 0 &&
                this.endDate != null &&
                LocalDateTime.now().isBefore(this.endDate);
    }

    public boolean hasLock(final User user) {
        return user.equals(this.user) && this.isActive();
    }

    public boolean isLockedBySomeoneElse(final User user) {
        return !user.equals(this.user) && this.isActive();
    }
}

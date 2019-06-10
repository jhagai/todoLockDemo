package fr.jhagai.todoLockDemo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "TODO_LOCK")
public class TodoLock {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Long count;

    @ManyToOne(optional = false)
    private User user;

    @OneToOne
    @MapsId
    @JoinColumn(name = "ID")
    private Todo todo;
}

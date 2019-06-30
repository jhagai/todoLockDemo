package fr.jhagai.todoLockDemo.entities;

import fr.jhagai.todoLockDemo.utils.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Embeddable
public class TodoLock {

    @Convert(converter = LocalDateTimeConverter.class)
    @Column()
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Long count;

    @ManyToOne(optional = true)
    private User user;
}

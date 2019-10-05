package fr.jhagai.todoLockDemo.core.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "TodoLockToken")
public class TodoLockToken {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    private Long id;

    @ManyToOne
    private TodoLock todoLock;
}

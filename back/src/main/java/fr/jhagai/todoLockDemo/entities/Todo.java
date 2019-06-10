package fr.jhagai.todoLockDemo.entities;

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

    @OneToOne(mappedBy = "todo")
    private TodoLock todoLock;
}

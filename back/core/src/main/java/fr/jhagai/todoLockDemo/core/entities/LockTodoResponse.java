package fr.jhagai.todoLockDemo.core.entities;

import fr.jhagai.todoLockDemo.core.dto.TodoDto;
import lombok.Data;

@Data
public class LockTodoResponse {
    private TodoDto todoDto;
    private Long token;
}

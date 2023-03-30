package ru.practicum.shareit.common.exception;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EntityNotFoundException extends RuntimeException {

    private Class<?> entityClass;

    public EntityNotFoundException(String message, Class<?> entityClass) {
        super(message);
        this.entityClass = entityClass;
    }
}

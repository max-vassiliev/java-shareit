package ru.practicum.shareit.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static boolean isNameNotNull(User user) {
        return user.getName() != null && !user.getName().isBlank();
    }

    public static boolean isEmailNotNull(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank();
    }
}

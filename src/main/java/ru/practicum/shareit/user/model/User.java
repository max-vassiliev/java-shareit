package ru.practicum.shareit.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    private Set<Long> userItems = new HashSet<>();

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public boolean isNameNotNull(User user) {
        return user.getName() != null && !user.getName().isBlank();
    }

    public boolean isEmailNotNull(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank();
    }
}

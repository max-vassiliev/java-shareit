package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private Long nextId = 1L;
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emails = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(Long userId) {
        validateId(userId);
        return users.get(userId);
    }

    @Override
    public User saveUser(User user) {
        validateEmail(user.getEmail());
        user.setId(nextId++);

        users.put(user.getId(), user);
        emails.put(user.getEmail(), user.getId());

        return users.get(user.getId());
    }

    @Override
    public User updateUser(User updatedUser) {
        User user = findUserById(updatedUser.getId());

        if (updatedUser.isNameNotNull(updatedUser)) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.isEmailNotNull(updatedUser)) {
            validateEmail(updatedUser.getEmail());
            updateEmail(user, updatedUser);
            user.setEmail(updatedUser.getEmail());
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        emails.remove(user.getEmail());
        users.remove(userId);
    }


    // ----------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ----------------------

    private void validateId(Long userId) {
        if (users.get(userId) == null) {
            throw new EntityNotFoundException("Не найден пользователь с ID " + userId, User.class);
        }
    }

    private void validateEmail(String email) {
        if (emails.containsKey(email)) {
            throw new ConflictException("Адрес " + email + " уже есть в базе. Выберите другой адрес.");
        }
    }

    private void updateEmail(User oldUser, User updatedUser) {
        if (oldUser.getEmail().equals(updatedUser.getEmail())) return;
        validateEmail(updatedUser.getEmail());
        emails.remove(oldUser.getEmail());
        emails.put(updatedUser.getEmail(), updatedUser.getId());
    }
}
package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    List<User> getAllUsers();

    User findUserById(Long id);

    User saveUser(User user);

    User updateUser(User updatedUser);

    void deleteUser(Long id);

}

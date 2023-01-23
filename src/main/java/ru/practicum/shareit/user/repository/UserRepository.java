package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    List<User> getAll();

    User getById(Long id);

    User save(User user);

    User update(User updatedUser);

    void delete(Long id);

}

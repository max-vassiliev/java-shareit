package ru.practicum.shareit.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll(Pageable pageable);

    UserDto getById(Long id);

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto);

    void delete(Long id);

}

package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    protected final UserMapper userMapper;

    @Override
    public List<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        User user = getUser(id);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto) {
        User user = getUser(userDto.getId());
        updateFields(user, userDto);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + id,
                        User.class)
                );
    }

    private void updateFields(User user, UserDto userDto) {
        if (UserDto.isNameNotNull(userDto)) {
            user.setName(userDto.getName());
        }
        if (UserDto.isEmailNotNull(userDto)) {
            user.setEmail(userDto.getEmail());
        }
        userRepository.flush();
    }
}

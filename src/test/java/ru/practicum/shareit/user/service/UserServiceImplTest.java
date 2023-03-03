package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Spy
    private UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    UserServiceImpl userService;


    @Test
    void create_whenValid_returnOutputDtoWithUserId() {
        UserDto inputDto = createUserDto();
        User savedUser = createUser();

        when(userRepository.save(any()))
                .thenReturn(savedUser);

        UserDto outputDto = userService.create(inputDto);

        assertEquals(outputDto.getId(), savedUser.getId());
        assertEquals(outputDto.getName(), savedUser.getName());
        assertEquals(outputDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void create_whenEmailDuplicate_thenThrowInternalError() {
        UserDto inputDto = createUserDto();

        when(userRepository.save(any()))
                .thenThrow(InternalError.class);

        assertThrows(InternalError.class, () -> userService.create(inputDto));
    }

    @Test
    void update_whenValid_thenSaveAndReturnDto() {
        Long userId = 1L;
        UserDto inputDto = createUserDto();
        inputDto.setId(userId);
        User updatedUser = createUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(updatedUser));

        UserDto outputDto = userService.update(inputDto);

        assertEquals(outputDto.getId(), updatedUser.getId());
        assertEquals(outputDto.getName(), updatedUser.getName());
        assertEquals(outputDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserNotFound_thenEntityNotFoundExceptionThrown() {
        Long userId = 1L;
        UserDto inputDto = createUserDto();
        inputDto.setId(userId);

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.update(inputDto));
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
    }

    @Test
    void updateName_whenValid_thenReturnDto() {
        UserDto inputDto = new UserDto();
        inputDto.setId(1L);
        inputDto.setName("Peter");
        User updatedUser = createUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(updatedUser));

        UserDto outputDto = userService.update(inputDto);
        assertEquals(outputDto.getId(), updatedUser.getId());
        assertEquals(outputDto.getName(), updatedUser.getName());
    }

    @Test
    void updateEmail_whenValid_thenReturnDto() {
        UserDto inputDto = new UserDto();
        inputDto.setId(1L);
        inputDto.setEmail("peter@example.com");
        User updatedUser = createUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(updatedUser));

        UserDto outputDto = userService.update(inputDto);
        assertEquals(outputDto.getId(), updatedUser.getId());
        assertEquals(outputDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void getById_whenValid_thenReturnDto() {
        Long userId = 1L;
        UserDto expectedOutputDto = createUserDto();
        expectedOutputDto.setId(userId);
        User foundUser = createUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(foundUser));

        UserDto actualOutputDto = userService.getById(userId);
        assertEquals(actualOutputDto.getId(), expectedOutputDto.getId());
        assertEquals(actualOutputDto.getName(), expectedOutputDto.getName());
        assertEquals(actualOutputDto.getEmail(), expectedOutputDto.getEmail());
    }

    @Test
    void getById_whenUserNotFound_thenReturnEntityNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getById(userId));
        assertEquals(exception.getEntityClass(), User.class);
    }

    @Test
    void getAll_whenInvoked_returnUserDtos() {
        List<User> users = createUsers();
        final Page<User> foundUsers = new PageImpl<>(users);
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        when(userRepository.findAll(defaultPageable))
                .thenReturn(foundUsers);

        List<UserDto> foundUsersDtos = userService.getAll(defaultPageable);
        assertEquals(foundUsersDtos.size(), users.size());
        assertEquals(foundUsersDtos.get(0).getId(), users.get(0).getId());
        assertEquals(foundUsersDtos.get(0).getName(), users.get(0).getName());
        assertEquals(foundUsersDtos.get(0).getEmail(), users.get(0).getEmail());
        assertEquals(foundUsersDtos.get(1).getId(), users.get(1).getId());
        assertEquals(foundUsersDtos.get(1).getName(), users.get(1).getName());
        assertEquals(foundUsersDtos.get(1).getEmail(), users.get(1).getEmail());
        assertEquals(foundUsersDtos.get(2).getId(), users.get(2).getId());
        assertEquals(foundUsersDtos.get(2).getName(), users.get(2).getName());
        assertEquals(foundUsersDtos.get(2).getEmail(), users.get(2).getEmail());
    }

    @Test
    void delete_WhenInvoked_thenNothingReturned() {
        Long userID = 1L;
        doNothing().when(userRepository).deleteById(isA(Long.class));
        userService.delete(userID);
        verify(userRepository, times(1)).deleteById(any());
    }

    // ----------
    // Шаблоны
    // ----------

    private UserDto createUserDto() {
        UserDto dto = new UserDto();
        dto.setName("Peter");
        dto.setEmail("peter@example.com");
        return dto;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Peter");
        user.setEmail("peter@example.com");
        return user;
    }

    private List<User> createUsers() {
        return new ArrayList<>(Arrays.asList(
                new User(1L, "Peter", "peter@example.com"),
                new User(2L, "Kate", "kate@example.com"),
                new User(3L, "Paul", "paul@example.com")
        ));
    }
}
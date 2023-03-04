package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;


    @Test
    @SneakyThrows
    void create_whenValid_thenStatusIsOkAndDtoReturned() {
        UserDto inputDto = createUserDto();
        UserDto outputDto = createUserDto();
        outputDto.setId(1L);

        when(userService.create(any()))
                .thenReturn(outputDto);

        mvc.perform(post("/users")
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.email", is(outputDto.getEmail())));

        verify(userService, times(1)).create(any());
    }

    @Test
    @SneakyThrows
    void create_whenEmailDuplicate_thenReturnInternalServerError() {
        UserDto inputDto = createUserDto();

        when(userService.create(any()))
                .thenThrow(InternalError.class);

        mvc.perform(post("/users")
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).create(any());
    }

    @Test
    @SneakyThrows
    void create_whenEmailIsEmpty_thenReturnBadRequest() {
        UserDto inputDto = createUserDto();
        inputDto.setEmail(null);

        mvc.perform(post("/users")
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(inputDto);
    }

    @Test
    @SneakyThrows
    void create_whenEmailInvalid_thenReturnBadRequest() {
        UserDto inputDto = createUserDto();
        inputDto.setEmail("peter.com");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(inputDto);
    }

    @Test
    @SneakyThrows
    void update_whenValid_thenSaveAndReturnDto() {
        UserDto userUpdate = createUserDto();
        userUpdate.setId(1L);

        when(userService.update(any()))
                .thenReturn(userUpdate);

        mvc.perform(patch("/users/{userId}", userUpdate.getId())
                        .content(mapper.writeValueAsString(userUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userUpdate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdate.getName())))
                .andExpect(jsonPath("$.email", is(userUpdate.getEmail())));

        verify(userService, times(1)).update(any());
    }

    @Test
    @SneakyThrows
    void updateName_whenValid_thenReturnDto() {
        String newName = "Petr";
        Long userId = 1L;

        UserDto userUpdated = createUserDto();
        userUpdated.setName(newName);
        userUpdated.setId(userId);

        UserDto nameUpdate = new UserDto();
        nameUpdate.setName(newName);

        when(userService.update(any()))
                .thenReturn(userUpdated);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(nameUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdated.getName())))
                .andExpect(jsonPath("$.email", is(userUpdated.getEmail())));

        verify(userService, times(1)).update(any());
    }


    @Test
    @SneakyThrows
    void updateEmail_whenValid_thenReturnDto() {
        String newEmail = "peter-update@example.com";
        Long userId = 1L;

        UserDto userUpdated = createUserDto();
        userUpdated.setEmail(newEmail);
        userUpdated.setId(userId);

        UserDto newEmailDto = new UserDto();
        newEmailDto.setEmail(newEmail);

        when(userService.update(any()))
                .thenReturn(userUpdated);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(newEmailDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdated.getName())))
                .andExpect(jsonPath("$.email", is(userUpdated.getEmail())));

        verify(userService, times(1)).update(any());
    }

    @Test
    @SneakyThrows
    void updateEmail_whenEmailNotUnique_thenReturnInternalServerError() {
        Long userId = 1L;
        UserDto newEmailDto = new UserDto();
        newEmailDto.setEmail("peter@example.com");

        when(userService.update(any()))
                .thenThrow(InternalError.class);

        mvc.perform(patch("/users/{userId}", userId)
                    .content(mapper.writeValueAsString(newEmailDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).update(any());
    }

    @Test
    @SneakyThrows
    void getById_whenValid_thenReturnDto() {
        Long userId = 1L;
        UserDto outputDto = createUserDto();
        outputDto.setId(userId);

        when(userService.getById(any()))
                .thenReturn(outputDto);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.email", is(outputDto.getEmail())));

        verify(userService, times(1)).getById(any());
    }

    @Test
    @SneakyThrows
    void getById_whenUserNotFound_thenReturnNotFound() {
        Long userId = 100L;

        when(userService.getById(any()))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getById(any());
    }

    @Test
    @SneakyThrows
    void delete_whenInvoked_thenReturnStatusOk() {
        mvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(any());
    }

    @Test
    @SneakyThrows
    void getAll_whenInvoked_returnUserDtos() {
        List<UserDto> userDtos = createUserDtos();

        when(userService.getAll(any()))
                .thenReturn(userDtos);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(userDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].email", is(userDtos.get(0).getEmail())))
                .andExpect(jsonPath("$[1].id", is(userDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(userDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].email", is(userDtos.get(1).getEmail())))
                .andExpect(jsonPath("$[2].id", is(userDtos.get(2).getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(userDtos.get(2).getName())))
                .andExpect(jsonPath("$[2].email", is(userDtos.get(2).getEmail())));

        verify(userService, times(1)).getAll(any());
    }

    @Test
    @SneakyThrows
    void getAll_whenFromIs2SizeIs2AndAll3_thenReturnListWithOneUserDto() {
        List<UserDto> dtos = createUserDtos();
        int from = 2;
        int size = 2;

        when(userService.getAll(any()))
                .thenReturn(Collections.singletonList(dtos.get(from)));

        mvc.perform(get("/users?from={from}&size={size}", from, size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtos.get(from).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dtos.get(from).getName())))
                .andExpect(jsonPath("$[0].email", is(dtos.get(from).getEmail())));
    }

    @Test
    @SneakyThrows
    void getAll_whenSizeIsZero_returnInternalServerError() {
        int size = 0;

        mvc.perform(get("/users?size={size}", size))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getAll(any());
    }

    @Test
    @SneakyThrows
    void getAll_whenFromIsNegative_returnInternalServerError() {
        int from = -1;

        mvc.perform(get("/users?from={from}", from))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getAll(any());
    }

    @Test
    @SneakyThrows
    void getAll_whenSizeIsNegative_returnInternalServerError() {
        int size = -1;

        mvc.perform(get("/users?size={size}", size))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getAll(any());
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

    private List<UserDto> createUserDtos() {
        return new ArrayList<>(Arrays.asList(
                new UserDto(1L, "Peter", "peter@example.com"),
                new UserDto(2L, "Kate", "kate@example.com"),
                new UserDto(3L, "Paul", "paul@example.com")
        ));
    }
}
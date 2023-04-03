package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.isA;
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

    private static final String PATH = "/users";

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;


    @Nested
    class Post {
        @SneakyThrows
        private RequestBuilder buildRequest(UserDto inputDto) {
            return post(PATH)
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        }

        @Test
        @SneakyThrows
        void create_whenValid_thenStatusIsOkAndDtoReturned() {
            UserDto inputDto = createUserDto();
            UserDto outputDto = createUserDto();
            outputDto.setId(1L);

            when(userClient.create(isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

            ResultActions result =  mvc.perform(buildRequest(inputDto))
                    .andExpect(status().isOk());
            checkFields(result, outputDto);

            verify(userClient, times(1))
                    .create(isA(UserDto.class));
        }

        @Test
        @SneakyThrows
        void create_whenEmailIsEmpty_thenReturnBadRequest() {
            UserDto inputDto = createUserDto();
            inputDto.setEmail(null);

            mvc.perform(buildRequest(inputDto))
                    .andExpect(status().isBadRequest());

            verify(userClient, never()).create(inputDto);
        }

        @Test
        @SneakyThrows
        void create_whenEmailInvalid_thenReturnBadRequest() {
            UserDto inputDto = createUserDto();
            inputDto.setEmail("peter.com");

            mvc.perform(buildRequest(inputDto))
                    .andExpect(status().isBadRequest());

            verify(userClient, never()).create(inputDto);
        }

        @Test
        @SneakyThrows
        void create_whenEmailDuplicate_thenReturnInternalServerError() {
            UserDto inputDto = createUserDto();

            when(userClient.create(isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

            mvc.perform(buildRequest(inputDto))
                    .andExpect(status().isInternalServerError());

            verify(userClient, times(1))
                    .create(isA(UserDto.class));
        }
    }

    @Nested
    class Patch {
        @SneakyThrows
        private RequestBuilder buildRequest(UserDto inputDto) {
            return patch(PATH + "/{userId}", inputDto.getId())
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        }

        @Test
        @SneakyThrows
        void update_whenValid_thenSaveAndReturnDto() {
            UserDto userUpdate = createUserDto();
            userUpdate.setId(1L);

            when(userClient.update(isA(Long.class), isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(userUpdate, HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(userUpdate))
                    .andExpect(status().isOk());
            checkFields(result, userUpdate);

            verify(userClient, times(1))
                    .update(isA(Long.class), isA(UserDto.class));
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
            nameUpdate.setId(userId);

            when(userClient.update(isA(Long.class), isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(userUpdated, HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(nameUpdate))
                    .andExpect(status().isOk());
            checkFields(result, userUpdated);

            verify(userClient, times(1))
                    .update(isA(Long.class), isA(UserDto.class));
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
            newEmailDto.setId(userId);

            when(userClient.update(isA(Long.class), isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(userUpdated, HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(newEmailDto))
                    .andExpect(status().isOk());
            checkFields(result, userUpdated);

            verify(userClient, times(1))
                    .update(isA(Long.class), isA(UserDto.class));
        }

        @Test
        @SneakyThrows
        void updateEmail_whenEmailNotUnique_thenReturnInternalServerError() {
            Long userId = 1L;
            UserDto newEmailDto = new UserDto();
            newEmailDto.setEmail("peter@example.com");
            newEmailDto.setId(userId);

            when(userClient.update(isA(Long.class), isA(UserDto.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

            mvc.perform(buildRequest(newEmailDto))
                    .andExpect(status().isInternalServerError());

            verify(userClient, times(1))
                    .update(isA(Long.class), isA(UserDto.class));
        }
    }

    @Nested
    class GetById {
        @SneakyThrows
        private RequestBuilder buildRequest(Long userId) {
            return get(PATH + "/{userId}", userId);
        }

        @Test
        @SneakyThrows
        void getById_whenValid_thenReturnDto() {
            Long userId = 1L;
            UserDto outputDto = createUserDto();
            outputDto.setId(userId);

            when(userClient.getById(isA(Long.class)))
                    .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(userId))
                    .andExpect(status().isOk());
            checkFields(result, outputDto);

            verify(userClient, times(1))
                    .getById(isA(Long.class));
        }

        @Test
        @SneakyThrows
        void getById_whenUserNotFound_thenReturnNotFound() {
            Long userId = 100L;

            when(userClient.getById(isA(Long.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

            mvc.perform(buildRequest(userId))
                    .andExpect(status().isNotFound());

            verify(userClient, times(1))
                    .getById(isA(Long.class));
        }
    }

    @Nested
    class GetAll {
        @SneakyThrows
        private RequestBuilder buildRequest(Integer from, Integer size) {
            if (from != null && size != null) {
                return get(PATH + "?from={from}&size={size}", from, size);
            }
            if (from != null) {
                return get(PATH + "?from={from}", from);
            }
            if (size != null) {
                return get(PATH + "?size={size}", size);
            }
            return get(PATH);
        }

        @Test
        @SneakyThrows
        void getAll_whenInvoked_returnUserDtos() {
            List<UserDto> userDtos = createUserDtos();

            when(userClient.getAll(isA(Integer.class), isA(Integer.class)))
                    .thenReturn(new ResponseEntity<>(userDtos, HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(null, null))
                    .andExpect(status().isOk());
            checkFields(result, userDtos);

            verify(userClient, times(1))
                    .getAll(isA(Integer.class), isA(Integer.class));
        }

        @Test
        @SneakyThrows
        void getAll_whenFromIs2SizeIs2AndAll3_thenReturnListWithOneUserDto() {
            int from = 2;
            int size = 2;
            List<UserDto> dtos = createUserDtos();
            List<UserDto> output = Collections.singletonList(dtos.get(from));

            when(userClient.getAll(isA(Integer.class), isA(Integer.class)))
                    .thenReturn(new ResponseEntity<>(Collections.singletonList(dtos.get(from)), HttpStatus.OK));

            ResultActions result = mvc.perform(buildRequest(from, size))
                    .andExpect(status().isOk());
            checkFields(result, output);

            verify(userClient, times(1))
                    .getAll(isA(Integer.class), isA(Integer.class));
        }

        @Test
        @SneakyThrows
        void getAll_whenSizeIsZero_returnBadRequest() {
            int size = 0;

            mvc.perform(buildRequest(null, size))
                    .andExpect(status().isBadRequest());

            verify(userClient, never())
                    .getAll(isA(Integer.class), isA(Integer.class));
        }

        @Test
        @SneakyThrows
        void getAll_whenFromIsNegative_returnBadRequest() {
            int from = -1;

            mvc.perform(buildRequest(from, null))
                    .andExpect(status().isBadRequest());

            verify(userClient, never())
                    .getAll(isA(Integer.class), isA(Integer.class));
        }

        @Test
        @SneakyThrows
        void getAll_whenSizeIsNegative_returnBadRequest() {
            int size = -1;

            mvc.perform(buildRequest(null, size))
                    .andExpect(status().isBadRequest());

            verify(userClient, never())
                    .getAll(isA(Integer.class), isA(Integer.class));
        }
    }

    @Test
    @SneakyThrows
    void delete_whenInvoked_thenReturnStatusOk() {
        mvc.perform(delete(PATH + "/{id}", 1L))
                .andExpect(status().isOk());

        verify(userClient, times(1)).delete(isA(Long.class));
    }


    // ----------------------
    // Вспомогательные методы
    // ----------------------

    @SneakyThrows
    private void checkFields(ResultActions readResult, UserDto outputDto) {
        readResult
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.email", is(outputDto.getEmail())));
    }

    @SneakyThrows
    private void checkFields(ResultActions readResult, List<UserDto> outputDtos) {
        readResult
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(outputDtos.size())));

        for (int i = 0; i < outputDtos.size(); i++) {
            readResult
                    .andExpect(jsonPath("$[" + i + "].id", is(outputDtos.get(i).getId()), Long.class))
                    .andExpect(jsonPath("$[" + i + "].name", is(outputDtos.get(i).getName())))
                    .andExpect(jsonPath("$[" + i + "].email", is(outputDtos.get(i).getEmail())));
        }
    }

    // ---------
    // Шаблоны
    // ---------

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
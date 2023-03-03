package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserSpringBootTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    User user1;
    User user2;
    User user3;


    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        user3 = userRepository.save(createUser("Paul", "paul@example.com"));
    }



    // --------
    // Тесты
    // --------

    @Test
    @SneakyThrows
    void create_whenValid_thenUserCreatedAndDtoReturned() {
        UserDto inputDto = createInputDto("peter2@example.com");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.email", is(inputDto.getEmail())));
    }

    @Test
    @SneakyThrows
    void create_whenEmailNotUnique_thenStatusIsInternalServerError() {
        UserDto inputDto = createInputDto("peter@example.com");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void update_whenEmailNotUnique_thenStatusIsInternalServerError() {
        Long userId = user3.getId();
        UserDto newEmailDto = new UserDto();
        newEmailDto.setEmail(user1.getEmail());

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(newEmailDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void getAll_whenAllIs3AndDefaultPageable_then3UsersReturned() {
        int expectedUsers = 3;

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedUsers)))
                .andExpect(jsonPath("$[0].id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$[2].id", is(user3.getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAll_whenAllIs3FromIs1AndSizeIs1_thenListWith1UserReturned() {
        int expectedUsers = 1;
        String from = "1";
        String size = "1";

        mvc.perform(get("/users")
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedUsers)))
                .andExpect(jsonPath("$[0].id", is(user2.getId()), Long.class));
    }

    // ------------------------
    // Вспомогательные методы
    // ------------------------

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private UserDto createInputDto(String email) {
        UserDto userDto = new UserDto();
        userDto.setName("Peter II");
        userDto.setEmail(email);
        return userDto;
    }
}
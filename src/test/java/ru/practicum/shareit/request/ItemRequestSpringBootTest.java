package ru.practicum.shareit.request;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ItemRequestSpringBootTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    ItemRequestRepository requestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    ItemRequest request1;
    ItemRequest request2;
    ItemRequest request3;
    ItemRequest request4;
    ItemRequest request5;

    User user1;
    User user2;
    User user3;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        user3 = userRepository.save(createUser("Paul", "paul@example.com"));

        request1 = requestRepository.save(createRequest(user2,
                "Kate's Item Request 1", LocalDateTime.now().minusDays(5)));
        request2 = requestRepository.save(createRequest(user2,
                "Kate's Item Request 2", LocalDateTime.now().minusDays(4)));
        request3 = requestRepository.save(createRequest(user1,
                "Peter's Item Request 1", LocalDateTime.now().minusDays(3)));
        request4 = requestRepository.save(createRequest(user1,
                "Peter's Item Request 2", LocalDateTime.now().minusDays(2)));
        request5 = requestRepository.save(createRequest(user3,
                "Paul's Item Request 1", LocalDateTime.now().minusDays(1)));
    }

    @AfterEach
    void deleteAll() {
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @SneakyThrows
    void getAllByRequestorId_whenAllIs2_then2RequestsReturned() {
        int expectedRequests = 2;
        Long requestorId = request1.getRequestor().getId();

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedRequests)))
                .andExpect(jsonPath("$[0].id", is(request2.getId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", is(request2.getRequestor().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(request1.getId()), Long.class))
                .andExpect(jsonPath("$[1].requestorId", is(request1.getRequestor().getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIs3AndDefaultPageable_then3RequestsReturned() {
        int expectedRequests = 3;
        Long requestorId = request1.getRequestor().getId();

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedRequests)))
                .andExpect(jsonPath("$[0].id", is(request5.getId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(requestorId), Long.class))
                .andExpect(jsonPath("$[1].id", is(request4.getId()), Long.class))
                .andExpect(jsonPath("$[1].requestorId", not(requestorId), Long.class))
                .andExpect(jsonPath("$[2].id", is(request3.getId()), Long.class))
                .andExpect(jsonPath("$[2].requestorId", not(requestorId), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIs3FromIs1AndSizeIs1_thenListWith1RequestReturned() {
        int expectedRequests = 1;
        String from = "1";
        String size = "1";
        Long requestorId = request1.getRequestor().getId();

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, requestorId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedRequests)))
                .andExpect(jsonPath("$[0].id", is(request4.getId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(requestorId), Long.class));
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private ItemRequest createRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);
        return request;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
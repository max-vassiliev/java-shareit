package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "created");

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10, DEFAULT_SORT);

    private ItemRequest request1;

    private ItemRequest request2;

    private ItemRequest request3;

    private ItemRequest request4;

    private ItemRequest request5;


    @BeforeEach
    void setUp() {
        User user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        User user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        User user3 = userRepository.save(createUser("Paul", "paul@example.com"));

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
    void getAllByRequestorIdOrderByCreatedDesc_whenFound_thenRequestsListReturned() {
        int expectedRequests = 2;
        Long requestorId = request1.getRequestor().getId();

        List<ItemRequest> requests = requestRepository.getAllByRequestorIdOrderByCreatedDesc(requestorId);

        assertEquals(expectedRequests, requests.size());
        assertEquals(requestorId, request2.getRequestor().getId());
        assertTrue(request1.getCreated().isBefore(request2.getCreated()));
        assertEquals(request2, requests.get(0));
        assertEquals(request1, requests.get(1));
    }

    @Test
    void getAllByRequestorIdOrderByCreatedDesc_whenNoRequests_thenEmptyListReturned() {
        int expectedRequests = 0;
        Long requestorId = request5.getRequestor().getId();
        requestRepository.delete(request5);

        List<ItemRequest> requests = requestRepository.getAllByRequestorIdOrderByCreatedDesc(requestorId);

        assertEquals(expectedRequests, requests.size());
    }

    @Test
    void findAllByOtherUsers_whenCommentsExist_thenRequestsListReturned() {
        int expectedRequests = 3;
        Long requestorId = request1.getRequestor().getId();

        List<ItemRequest> requests = requestRepository.findAllByOtherUsers(requestorId, DEFAULT_PAGEABLE);

        assertEquals(expectedRequests, requests.size());
        assertNotEquals(requestorId, requests.get(0).getId());
        assertNotEquals(requestorId, requests.get(1).getId());
        assertNotEquals(requestorId, requests.get(2).getId());
        assertEquals(request5, requests.get(0));
        assertEquals(request4, requests.get(1));
        assertEquals(request3, requests.get(2));
    }

    @Test
    void findAllByOtherUsers_whenAllIs3FromIs1AndSizeIs1_thenListWith1RequestReturned() {
        int expectedRequests = 1;
        Long requestorId = request1.getRequestor().getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        List<ItemRequest> requests = requestRepository.findAllByOtherUsers(requestorId, customPageable);

        assertEquals(expectedRequests, requests.size());
        assertNotEquals(requestorId, requests.get(0).getId());
        assertEquals(request4, requests.get(0));
    }

    @Test
    void findAllByOtherUsers_whenNoRequestsByOtherUsers_thenEmptyListReturned() {
        int expectedRequests = 0;
        Long requestorId = request1.getRequestor().getId();
        requestRepository.delete(request5);
        requestRepository.delete(request4);
        requestRepository.delete(request3);

        List<ItemRequest> requests = requestRepository.findAllByOtherUsers(requestorId, DEFAULT_PAGEABLE);

        assertEquals(expectedRequests, requests.size());
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
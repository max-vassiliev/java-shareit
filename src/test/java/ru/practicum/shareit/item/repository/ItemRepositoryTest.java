package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRequestRepository requestRepository;

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10, Sort.by("id"));

    Item user1Item1;
    Item user1Item2;
    Item user1Item3;
    Item user2Item1;
    Item user2Item2;
    User user1;
    User user2;
    User user3;
    ItemRequest request1;
    ItemRequest request2;


    @BeforeEach
    public void setUp() {
        user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        user3 = userRepository.save(createUser("Paul", "paul@example.com"));

        request1 = requestRepository.save(createItemRequest("Item Request 1",
                user2, LocalDateTime.now().minusDays(2)));
        request2 = requestRepository.save(createItemRequest("Item Request 2",
                user3, LocalDateTime.now().minusDays(1)));

        user1Item1 = itemRepository.save(createItem("Peter's Item 1",
                "Peter's Item 1 Description", user1, null));
        user1Item2 = itemRepository.save(createItem("Peter's Item 2",
                "Peter's Item 2 Description (keyword)", user1, null));
        user1Item3 = itemRepository.save(createItem("Peter's Item 3 (keyword)",
                "Peter's Item 3 Description", user1, request1));
        user2Item1 = itemRepository.save(createItem("Kate's Item 1",
                "Kate's Item 1 Description", user2, request2));
        user2Item2 = itemRepository.save(createItem("Kate's Item 2",
                "Kate's Item 2 Description (keyword)", user2, null));
    }

    @AfterEach
    void deleteAll() {
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------
    // Тесты
    // ---------

    @Test
    void findByOwnerId_whenValid_thenItemsListReturned() {
        int expectedCount = 3;
        Long ownerId = user1.getId();

        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, ownerItems.size());
        assertEquals(ownerId, ownerItems.get(0).getOwner().getId());
        assertEquals(ownerId, ownerItems.get(1).getOwner().getId());
        assertEquals(ownerId, ownerItems.get(2).getOwner().getId());
    }

    @Test
    void findByOwnerId_whenFromIs2WithTotalOwnerItems3_thenListWithOneItemReturned() {
        int expectedCount = 1;
        int from = 2;
        Long ownerId = user1.getId();
        Pageable pageable = new CustomPageRequest(from, 10, Sort.by("id"));

        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId, pageable);
        assertEquals(expectedCount, ownerItems.size());
    }

    @Test
    void findByOwnerId_whenSizeIs1_thenListWithOneItemReturned() {
        int expectedCount = 1;
        int size = 1;
        Long ownerId = user1.getId();
        Pageable pageable = new CustomPageRequest(0, size, Sort.by("id"));

        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId, pageable);
        assertEquals(expectedCount, ownerItems.size());
    }

    @Test
    void searchByKeyword_whenValid_thenItemsListReturned() {
        String keyword = "keYwOrd";
        int expectedCount = 3;

        List<Item> foundItems = itemRepository.searchByKeyword(keyword, DEFAULT_PAGEABLE);
        assertEquals(expectedCount, foundItems.size());
    }

    @Test
    void searchByKeyword_whenAnyItemNotAvailable_thenAvailableItemsListReturned() {
        String keyword = "keYwOrd";
        int expectedCount = 3;
        int expectedCountAfterUpdate = 2;

        List<Item> foundItems = itemRepository.searchByKeyword(keyword, DEFAULT_PAGEABLE);
        assertEquals(expectedCount, foundItems.size());

        user2Item2.setIsAvailable(false);

        List<Item> foundItemsAfterUpdate = itemRepository.searchByKeyword(keyword, DEFAULT_PAGEABLE);
        assertEquals(expectedCountAfterUpdate, foundItemsAfterUpdate.size());
    }

    @Test
    void searchByKeyword_whenFromIs2AndTotalItemsIs3_thenListWithOneItemReturned() {
        String keyword = "keYwOrd";
        int customFrom = 2;
        int expectedCountDefault = 3;
        int expectedCountCustom = 1;

        Pageable pageableCustom = new CustomPageRequest(customFrom, 10, Sort.by("id"));

        List<Item> foundItemsDefault = itemRepository.searchByKeyword(keyword, DEFAULT_PAGEABLE);
        List<Item> foundItemsCustom = itemRepository.searchByKeyword(keyword, pageableCustom);

        assertEquals(expectedCountDefault, foundItemsDefault.size());
        assertEquals(expectedCountCustom, foundItemsCustom.size());
    }

    @Test
    void searchByKeyword_whenSizeIs1_thenListWithOneItemReturned() {
        String keyword = "keYwOrd";
        int customSize = 1;
        int expectedCountDefault = 3;
        int expectedCountCustom = 1;
        Pageable customPageable = new CustomPageRequest(0, customSize, Sort.by("id"));

        List<Item> foundItemsDefault = itemRepository.searchByKeyword(keyword, DEFAULT_PAGEABLE);
        List<Item> foundItemsCustom = itemRepository.searchByKeyword(keyword, customPageable);

        assertEquals(expectedCountDefault, foundItemsDefault.size());
        assertEquals(expectedCountCustom, foundItemsCustom.size());
    }

    @Test
    void findAllByRequestId_whenValid_thenItemsListReturned() {
        int expectedCount = 1;
        Long requestId = request1.getId();

        List<Item> foundItems = itemRepository.findAllByRequestId(requestId);
        assertEquals(expectedCount, foundItems.size());
        assertEquals(requestId, foundItems.get(0).getRequest().getId());
    }

    @Test
    void findAllByRequestIds_whenValid_thenItemsListReturned() {
        int expectedCount = 2;
        List<Long> requestIds = new ArrayList<>(Arrays.asList(request1.getId(), request2.getId()));

        List<Item> foundItems = itemRepository.findAllByRequestIds(requestIds);

        assertEquals(expectedCount, foundItems.size());
        assertEquals(request1.getId(), foundItems.get(0).getRequest().getId());
        assertEquals(request2.getId(), foundItems.get(1).getRequest().getId());
    }

    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private Item createItem(String name, String description, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest createItemRequest(String description, User requestor, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);
        return request;
    }
}
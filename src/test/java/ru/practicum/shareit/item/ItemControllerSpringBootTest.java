package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ItemControllerSpringBootTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ItemController itemController;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MockMvc mvc;

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
    Booking lastBookingItem1;
    Booking nextBookingItem1;
    Comment item1Comment;


    @AfterEach
    public void resetDb() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        requestRepository.deleteAll();
        commentRepository.deleteAll();
    }


    @Test
    @SneakyThrows
    void contextLoads() {
        assertThat(itemController).isNotNull();
        assertThat(itemRepository).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(requestRepository).isNotNull();
        assertThat(bookingRepository).isNotNull();
        assertThat(commentRepository).isNotNull();
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs3AndDefaultPageable_then3ItemsReturned() {
        setUp();
        int expectedItems = 3;
        Long ownerId = user1.getId();

        mvc.perform(get("/items")
                    .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(user1Item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1Item1.getName())))
                .andExpect(jsonPath("$[0].description", is(user1Item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(user1Item1.getIsAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(user1Item1.getRequest().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking").isNotEmpty())
                .andExpect(jsonPath("$[0].lastBooking.id",
                        is(lastBookingItem1.getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.start",
                        is(lastBookingItem1.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].lastBooking.end",
                        is(lastBookingItem1.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].lastBooking.itemId",
                        is(lastBookingItem1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.itemName",
                        is(lastBookingItem1.getItem().getName())))
                .andExpect(jsonPath("$[0].lastBooking.bookerId",
                        is(lastBookingItem1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.status",
                        is(lastBookingItem1.getStatus().toString())))
                .andExpect(jsonPath("$[0].nextBooking").isNotEmpty())
                .andExpect(jsonPath("$[0].nextBooking.id",
                        is(nextBookingItem1.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.start",
                        is(nextBookingItem1.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].nextBooking.end",
                        is(nextBookingItem1.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].nextBooking.itemId",
                        is(nextBookingItem1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.itemName",
                        is(nextBookingItem1.getItem().getName())))
                .andExpect(jsonPath("$[0].nextBooking.bookerId",
                        is(nextBookingItem1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.status",
                        is(nextBookingItem1.getStatus().toString())))
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].comments[0].id",
                        is(item1Comment.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text",
                        is(item1Comment.getText())))
                .andExpect(jsonPath("$[0].comments[0].authorId",
                        is(item1Comment.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].authorName",
                        is(item1Comment.getAuthor().getName())))
                .andExpect(jsonPath("$[0].comments[0].created",
                        is(item1Comment.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].id", is(user1Item2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user1Item2.getName())))
                .andExpect(jsonPath("$[1].description", is(user1Item2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(user1Item2.getIsAvailable())))
                .andExpect(jsonPath("$[2].id", is(user1Item3.getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(user1Item3.getName())))
                .andExpect(jsonPath("$[2].description", is(user1Item3.getDescription())))
                .andExpect(jsonPath("$[2].available", is(user1Item3.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs3FromIs1AndSizeIs1_thenListWith1ItemReturned() {
        setUp();
        int expectedItems = 1;
        Long ownerId = user1.getId();
        String from = "1";
        String size = "1";

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(user1Item2.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1Item2.getName())))
                .andExpect(jsonPath("$[0].description", is(user1Item2.getDescription())))
                .andExpect(jsonPath("$[0].available", is(user1Item2.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3AndDefaultPageable_then3ItemsReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 3;
        Long userId = user3.getId();

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(user1Item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1Item1.getName())))
                .andExpect(jsonPath("$[0].description", is(user1Item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(user1Item1.getIsAvailable())))
                .andExpect(jsonPath("$[1].id", is(user1Item3.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user1Item3.getName())))
                .andExpect(jsonPath("$[1].description", is(user1Item3.getDescription())))
                .andExpect(jsonPath("$[1].available", is(user1Item3.getIsAvailable())))
                .andExpect(jsonPath("$[2].id", is(user2Item2.getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(user2Item2.getName())))
                .andExpect(jsonPath("$[2].description", is(user2Item2.getDescription())))
                .andExpect(jsonPath("$[2].available", is(user2Item2.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3And1Unavailable_then2ItemsReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 2;
        Long userId = user3.getId();
        user2Item2.setIsAvailable(false);

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(user1Item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1Item1.getName())))
                .andExpect(jsonPath("$[0].description", is(user1Item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(user1Item1.getIsAvailable())))
                .andExpect(jsonPath("$[1].id", is(user1Item3.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user1Item3.getName())))
                .andExpect(jsonPath("$[1].description", is(user1Item3.getDescription())))
                .andExpect(jsonPath("$[1].available", is(user1Item3.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3FromIs1AndSizeIs1_thenListWith1ItemReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 1;
        String from = "1";
        String size = "1";
        Long userId = user3.getId();

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(user1Item3.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1Item3.getName())))
                .andExpect(jsonPath("$[0].description", is(user1Item3.getDescription())))
                .andExpect(jsonPath("$[0].available", is(user1Item3.getIsAvailable())));
    }


    // ----------
    // Шаблоны
    // ----------

    public void setUp() {
        user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        user3 = userRepository.save(createUser("Paul", "paul@example.com"));

        request1 = requestRepository.save(createItemRequest("Item Request 1",
                user2, LocalDateTime.now().minusDays(9)));
        request2 = requestRepository.save(createItemRequest("Item Request 2",
                user3, LocalDateTime.now().minusDays(7)));

        user1Item1 = itemRepository.save(createItem("Peter's Item 1",
                "Peter's Item 1 Description (keyword)", user1, request1));
        user1Item2 = itemRepository.save(createItem("Peter's Item 2",
                "Peter's Item 2 Description", user1, null));
        user1Item3 = itemRepository.save(createItem("Peter's Item 3 (keyword)",
                "Peter's Item 3 Description", user1, null));
        user2Item1 = itemRepository.save(createItem("Kate's Item 1",
                "Kate's Item 1 Description", user2, request2));
        user2Item2 = itemRepository.save(createItem("Kate's Item 2",
                "Kate's Item 2 Description (keyword)", user2, null));

        lastBookingItem1 = bookingRepository.save(createBooking(user2, user1Item1,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1)));
        nextBookingItem1 = bookingRepository.save(createBooking(user3, user1Item1,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)));

        item1Comment = commentRepository.save(createComment(
                user1Item1, user2, LocalDateTime.now().minusHours(4)));
    }

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

    private Booking createBooking(User booker, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }

    private Comment createComment(Item item, User author, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setText("Item 1 Comment By Kate");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);
        return comment;
    }
}
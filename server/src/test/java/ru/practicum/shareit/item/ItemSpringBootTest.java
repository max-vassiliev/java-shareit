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
class ItemSpringBootTest {

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

    private Item item1;

    private Item item2;

    private Item item3;

    private Item item4;

    private User peter;

    private User paul;

    private Booking lastBookingItem1;

    private Booking nextBookingItem1;

    private Comment item1Comment;


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
        Long ownerId = peter.getId();

        mvc.perform(get("/items")
                    .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(item1.getRequest().getId()), Long.class))
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
                .andExpect(jsonPath("$[1].id", is(item2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(item2.getName())))
                .andExpect(jsonPath("$[1].description", is(item2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item2.getIsAvailable())))
                .andExpect(jsonPath("$[2].id", is(item3.getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(item3.getName())))
                .andExpect(jsonPath("$[2].description", is(item3.getDescription())))
                .andExpect(jsonPath("$[2].available", is(item3.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs3FromIs1AndSizeIs1_thenListWith1ItemReturned() {
        setUp();
        int expectedItems = 1;
        Long ownerId = peter.getId();
        String from = "1";
        String size = "1";

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(item2.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item2.getName())))
                .andExpect(jsonPath("$[0].description", is(item2.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item2.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3AndDefaultPageable_then3ItemsReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 3;
        Long userId = paul.getId();

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$[1].id", is(item3.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(item3.getName())))
                .andExpect(jsonPath("$[1].description", is(item3.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item3.getIsAvailable())))
                .andExpect(jsonPath("$[2].id", is(item4.getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(item4.getName())))
                .andExpect(jsonPath("$[2].description", is(item4.getDescription())))
                .andExpect(jsonPath("$[2].available", is(item4.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3And1Unavailable_then2ItemsReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 2;
        Long userId = paul.getId();
        item4.setIsAvailable(false);

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$[1].id", is(item3.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(item3.getName())))
                .andExpect(jsonPath("$[1].description", is(item3.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item3.getIsAvailable())));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenAllIs3FromIs1AndSizeIs1_thenListWith1ItemReturned() {
        setUp();
        String keyword = "keYwOrd";
        int expectedItems = 1;
        String from = "1";
        String size = "1";
        Long userId = paul.getId();

        mvc.perform(get("/items/search?text={text}", keyword)
                        .header(USER_ID_HEADER, userId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].id", is(item3.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item3.getName())))
                .andExpect(jsonPath("$[0].description", is(item3.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item3.getIsAvailable())));
    }


    // ----------
    // Шаблоны
    // ----------

    public void setUp() {
        peter = userRepository.save(createUser("Peter", "peter@example.com"));
        paul = userRepository.save(createUser("Paul", "paul@example.com"));
        User kate = userRepository.save(createUser("Kate", "kate@example.com"));

        ItemRequest request1 = requestRepository.save(createItemRequest(
                kate, LocalDateTime.now().minusDays(9)));

        item1 = itemRepository.save(createItem("Peter's Item 1",
                "Peter's Item 1 Description (keyword)", peter, request1));
        item2 = itemRepository.save(createItem("Peter's Item 2",
                "Peter's Item 2 Description", peter, null));
        item3 = itemRepository.save(createItem("Peter's Item 3 (keyword)",
                "Peter's Item 3 Description", peter, null));
        item4 = itemRepository.save(createItem("Kate's Item 2",
                "Kate's Item 2 Description (keyword)", kate, null));

        lastBookingItem1 = bookingRepository.save(createBooking(kate, item1,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1)));
        nextBookingItem1 = bookingRepository.save(createBooking(paul, item1,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)));

        item1Comment = commentRepository.save(createComment(
                item1, kate, LocalDateTime.now().minusHours(4)));
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

    private ItemRequest createItemRequest(User requestor, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription("Item Request 1");
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
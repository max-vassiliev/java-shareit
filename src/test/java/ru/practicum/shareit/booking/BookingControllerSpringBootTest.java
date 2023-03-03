package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class BookingControllerSpringBootTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    Booking booking1;
    Booking booking2;
    Booking booking3;
    Booking booking4;

    User user1;
    User user2;
    User user3;

    Item item1;
    Item item2;
    Item item3;

    @BeforeEach
    public void setUp() {
        user1 = userRepository.save(createUser("Peter", "peter@example.com"));
        user2 = userRepository.save(createUser("Kate", "kate@example.com"));
        user3 = userRepository.save(createUser("Paul", "paul@example.com"));

        item1 = itemRepository.save(createItem(user1, "Peter's Item 1",
                "Peter's Item 1 Description"));
        item2 = itemRepository.save(createItem(user1, "Peter's Item 2",
                "Peter's Item 2 Description"));
        item3 = itemRepository.save(createItem(user2, "Kate's Item 1",
                "Kate's Item 1 Description"));

        booking1 = bookingRepository.save(createBooking(user2, item1,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)));
        booking2 = bookingRepository.save(createBooking(user3, item1,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7)));
        booking3 = bookingRepository.save(createBooking(user2, item2,
                LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8)));
        booking4 = bookingRepository.save(createBooking(user2, item1,
                LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(11)));
    }

    @AfterEach
    public void deleteAll() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @SneakyThrows
    void getAllByBookerId_whenAllIs3AndDefaultPageable_then3BookingReturned() {
        int expectedBookings = 3;
        Long bookerId = user2.getId();

        mvc.perform(get("/bookings")
                    .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking4.getId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(booking4.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(booking3.getId()), Long.class))
                .andExpect(jsonPath("$[1].bookerId", is(booking3.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[2].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[2].bookerId", is(booking1.getBooker().getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenStatusIsPast_thenListWithPastBookingsReturned() {
        int expectedBookings = 1;
        Long bookerId = user2.getId();
        String state = "past";
        booking1.setStart(LocalDateTime.now().minusDays(3));
        booking1.setEnd(LocalDateTime.now().minusDays(1));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(booking1.getBooker().getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenAllIsFromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long bookerId = user2.getId();
        String from = "1";
        String size = "1";

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking3.getId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(booking3.getBooker().getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs4AndDefaultPageable_then4BookingsReturned() {
        int expectedBookings = 4;
        Long ownerId = user1.getId();

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking4.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(booking3.getId()), Long.class))
                .andExpect(jsonPath("$[2].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[3].id", is(booking1.getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenStateIsPast_thenPastBookingsReturned() {
        int expectedBookings = 1;
        String state = "past";
        Long ownerId = user1.getId();
        booking1.setStart(LocalDateTime.now().minusDays(3));
        booking1.setEnd(LocalDateTime.now().minusDays(1));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs4FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        String from = "1";
        String size = "1";
        Long ownerId = user1.getId();

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedBookings)))
                .andExpect(jsonPath("$[0].id", is(booking3.getId()), Long.class));
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private Booking createBooking(User booker, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }

    private Item createItem(User owner, String name, String description) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
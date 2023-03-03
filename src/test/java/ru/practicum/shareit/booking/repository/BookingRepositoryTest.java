package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "start");

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10, DEFAULT_SORT);

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
    void findByBookerAndItemPast_whenValid_thenBookingsListReturned() {
        int expectedBookings = 1;
        booking1.setStart(LocalDateTime.now().minusDays(7));
        booking1.setStart(LocalDateTime.now().minusDays(5));
        booking2.setStart(LocalDateTime.now().minusDays(3));
        booking2.setStart(LocalDateTime.now().minusDays(1));

        List<Booking> bookings = bookingRepository.findByBookerAndItemPast(user2, item1, LocalDateTime.now());

        assertEquals(expectedBookings, bookings.size());
        assertEquals(user2, bookings.get(0).getBooker());
        assertEquals(item1, bookings.get(0).getItem());
        assertEquals(item1, booking1.getItem());
        assertEquals(item1, booking2.getItem());
        assertEquals(user2, booking1.getBooker());
        assertNotEquals(user2, booking2.getBooker());
    }

    @Test
    void findByBookerAndItemPast_whenNoPastItems_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findByBookerAndItemPast(user2, item1, LocalDateTime.now());

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBooker_whenFound_thenBookingsListReturned() {
        int expectedBookings = 3;

        List<Booking> bookings = bookingRepository.findAllByBooker(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking4, bookings.get(0));
        assertEquals(booking3, bookings.get(1));
        assertEquals(booking1, bookings.get(2));
    }

    @Test
    void findAllByBooker_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        List<Booking> bookings = bookingRepository.findAllByBooker(user2, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findAllByBooker_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findAllByBooker(user1, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerWaiting_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        booking1.setStatus(BookingState.WAITING);

        List<Booking> bookings = bookingRepository.findAllByBookerWaiting(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerWaiting_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        booking1.setStatus(BookingState.WAITING);
        booking3.setStatus(BookingState.WAITING);
        booking4.setStatus(BookingState.WAITING);

        List<Booking> bookings = bookingRepository.findAllByBookerWaiting(user2, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findAllByBookerWaiting_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findAllByBookerWaiting(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerRejected_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        booking1.setStatus(BookingState.REJECTED);

        List<Booking> bookings = bookingRepository.findAllByBookerRejected(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerRejected_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        booking1.setStatus(BookingState.REJECTED);
        booking3.setStatus(BookingState.REJECTED);
        booking4.setStatus(BookingState.REJECTED);

        List<Booking> bookings = bookingRepository.findAllByBookerRejected(user2, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findAllByBookerRejected_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findAllByBookerRejected(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerPast_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(3));
        booking1.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findAllByBookerPast(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerPast_whenEndIsNow_thenBookingReturned() {
        int expectedBookings = 1;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(3));
        booking1.setEnd(now);

        List<Booking> bookings = bookingRepository.findAllByBookerPast(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerPast_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.minusDays(9));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.minusDays(5));
        booking4.setStart(now.minusDays(3));
        booking4.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findAllByBookerPast(user2, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findAllByBookerPast_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findAllByBookerPast(user2, LocalDateTime.now(), DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerCurrent_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusMinutes(1));
        booking1.setEnd(now.plusMinutes(1));

        List<Booking> bookings = bookingRepository.findAllByBookerCurrent(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerCurrent_whenStartIsNow_thenBookingsFound() {
        int expectedBookings = 1;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now);
        booking1.setEnd(now.plusDays(2));

        List<Booking> bookings = bookingRepository.findAllByBookerCurrent(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerCurrent_whenEndIsNow_thenBookingsNotFound() {
        int expectedBookings = 0;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(1));
        booking1.setEnd(now);

        List<Booking> bookings = bookingRepository.findAllByBookerCurrent(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerCurrent_whenAllIs2FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.plusDays(9));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.plusDays(5));

        List<Booking> bookings = bookingRepository.findAllByBookerCurrent(user2, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByBookerCurrent_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;

        List<Booking> bookings = bookingRepository.findAllByBookerCurrent(user2, LocalDateTime.now(), DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findAllByBookerFuture_whenFound_thenBookingsListReturned() {
        int expectedBookings = 3;
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findAllByBookerFuture(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking4, bookings.get(0));
        assertEquals(booking3, bookings.get(1));
        assertEquals(booking1, bookings.get(2));
    }

    @Test
    void findAllByBookerFuture_whenStartIsNow_thenBookingNotReturned() {
        int expectedBookings = 2;
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now);

        List<Booking> futureBookings = bookingRepository.findAllByBookerFuture(user2, now, DEFAULT_PAGEABLE);
        List<Booking> allBookings = bookingRepository.findAllByBooker(user2, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, futureBookings.size());
        assertNotEquals(futureBookings.size(), allBookings.size());
        assertFalse(futureBookings.contains(booking1));
        assertTrue(futureBookings.contains(booking3));
        assertTrue(futureBookings.contains(booking4));
    }

    @Test
    void findAllByBookerFuture_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findAllByBookerFuture(user2, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findAllByBookerFuture_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.minusDays(9));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.minusDays(5));
        booking4.setStart(now.minusDays(3));
        booking4.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findAllByBookerFuture(user2, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerId_whenFound_thenBookingsListReturned() {
        int expectedBookings = 4;
        Long ownerId = user1.getId();

        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking4, bookings.get(0));
        assertEquals(booking3, bookings.get(1));
        assertEquals(booking2, bookings.get(2));
        assertEquals(booking1, bookings.get(3));
    }

    @Test
    void findAllByBooker_whenAllIs4FromIs1AndSizeIs2_thenListWith2BookingsReturned() {
        int expectedBookings = 2;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 2;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
        assertEquals(booking2, bookings.get(1));
    }

    @Test
    void findByOwnerId_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user2.getId();

        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdWaiting_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        booking1.setStatus(BookingState.WAITING);

        List<Booking> bookings = bookingRepository.findByOwnerIdWaiting(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdWaiting_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        booking1.setStatus(BookingState.WAITING);
        booking3.setStatus(BookingState.WAITING);
        booking4.setStatus(BookingState.WAITING);

        List<Booking> bookings = bookingRepository.findByOwnerIdWaiting(ownerId, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findByOwnerIdWaiting_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();

        List<Booking> bookings = bookingRepository.findByOwnerIdWaiting(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdRejected_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        booking1.setStatus(BookingState.REJECTED);

        List<Booking> bookings = bookingRepository.findByOwnerIdRejected(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdRejected_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);

        booking1.setStatus(BookingState.REJECTED);
        booking3.setStatus(BookingState.REJECTED);
        booking4.setStatus(BookingState.REJECTED);

        List<Booking> bookings = bookingRepository.findByOwnerIdRejected(ownerId, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findByOwnerIdRejected_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();

        List<Booking> bookings = bookingRepository.findByOwnerIdRejected(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdPast_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(3));
        booking1.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findByOwnerIdPast(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdPast_whenEndIsNow_thenBookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(3));
        booking1.setEnd(now);

        List<Booking> bookings = bookingRepository.findByOwnerIdPast(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdPast_whenAllIs3FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.minusDays(9));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.minusDays(5));
        booking4.setStart(now.minusDays(3));
        booking4.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findByOwnerIdPast(ownerId, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findByOwnerIdPast_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();

        List<Booking> bookings = bookingRepository.findByOwnerIdPast(ownerId, LocalDateTime.now(), DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdCurrent_whenFound_thenBookingsListReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusMinutes(1));
        booking1.setEnd(now.plusMinutes(1));

        List<Booking> bookings = bookingRepository.findByOwnerIdCurrent(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdCurrent_whenStartIsNow_thenBookingsFound() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now);
        booking1.setEnd(now.plusDays(2));

        List<Booking> bookings = bookingRepository.findByOwnerIdCurrent(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdCurrent_whenEndIsNow_thenBookingsNotFound() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now.minusDays(1));
        booking1.setEnd(now);

        List<Booking> bookings = bookingRepository.findByOwnerIdCurrent(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdCurrent_whenAllIs2FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.plusDays(9));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.plusDays(5));

        List<Booking> bookings = bookingRepository.findByOwnerIdCurrent(ownerId, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findByOwnerIdCurrent_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();

        List<Booking> bookings = bookingRepository.findByOwnerIdCurrent(ownerId, LocalDateTime.now(), DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findByOwnerIdFuture_whenFound_thenBookingsListReturned() {
        int expectedBookings = 4;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findByOwnerIdFuture(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking4, bookings.get(0));
        assertEquals(booking3, bookings.get(1));
        assertEquals(booking2, bookings.get(2));
        assertEquals(booking1, bookings.get(3));
    }

    @Test
    void findByOwnerIdFuture_whenStartIsNow_thenBookingNotReturned() {
        int expectedBookings = 3;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();
        booking1.setStart(now);

        List<Booking> futureBookings = bookingRepository.findByOwnerIdFuture(ownerId, now, DEFAULT_PAGEABLE);
        List<Booking> allBookings = bookingRepository.findByOwnerId(ownerId, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, futureBookings.size());
        assertNotEquals(futureBookings.size(), allBookings.size());
        assertFalse(futureBookings.contains(booking1));
        assertTrue(futureBookings.contains(booking2));
        assertTrue(futureBookings.contains(booking3));
        assertTrue(futureBookings.contains(booking4));
    }

    @Test
    void findByOwnerIdFuture_whenAllIs4FromIs1AndSizeIs1_thenListWith1BookingReturned() {
        int expectedBookings = 1;
        Long ownerId = user1.getId();
        int from = 1;
        int size = 1;
        Pageable customPageable = new CustomPageRequest(from, size, DEFAULT_SORT);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findByOwnerIdFuture(ownerId, now, customPageable);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking3, bookings.get(0));
    }

    @Test
    void findByOwnerIdFuture_whenNotFound_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long ownerId = user1.getId();
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(11));
        booking1.setEnd(now.minusDays(9));
        booking2.setStart(now.minusDays(10));
        booking2.setEnd(now.minusDays(8));
        booking3.setStart(now.minusDays(7));
        booking3.setEnd(now.minusDays(5));
        booking4.setStart(now.minusDays(3));
        booking4.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findByOwnerIdFuture(ownerId, now, DEFAULT_PAGEABLE);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findLastByItemId_whenFound_thenBookingReturned() {
        Long itemId = item1.getId();
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now.minusDays(3));
        booking1.setEnd(now.minusDays(1));

        Booking foundBooking = bookingRepository.findLastByItemId(itemId, now);

        assertEquals(booking1, foundBooking);
    }

    @Test
    void findLastByItemId_whenStartIsNow_thenBookingReturned() {
        Long itemId = item1.getId();
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now);
        booking1.setEnd(now.plusDays(1));

        Booking foundBooking = bookingRepository.findLastByItemId(itemId, now);

        assertEquals(booking1, foundBooking);
    }

    @Test
    void findLastByItemId_whenNoPriorBookings_thenNullReturned() {
        Long itemId = item1.getId();
        LocalDateTime now = LocalDateTime.now();

        Booking foundBooking = bookingRepository.findLastByItemId(itemId, now);

        assertNull(foundBooking);
    }

    @Test
    void findLastByItemId_whenNoBookings_thenNullReturned() {
        Long itemId = item3.getId();
        LocalDateTime now = LocalDateTime.now();

        Booking foundBooking = bookingRepository.findLastByItemId(itemId, now);

        assertNull(foundBooking);
    }

    @Test
    void findLastByItemIds_whenFound_thenBookingsListReturned() {
        int expectedBookings = 2;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        booking4.setStart(now.minusDays(8));
        booking4.setEnd(now.minusDays(7));
        booking3.setStart(now.minusDays(6));
        booking3.setEnd(now.minusDays(5));
        booking2.setStart(now.minusDays(4));
        booking2.setEnd(now.minusDays(3));
        booking1.setStart(now.minusDays(2));
        booking1.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findLastByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
        assertTrue(bookings.contains(booking1));
        assertTrue(bookings.contains(booking3));
    }

    @Test
    void findLastByItemIds_whenStartIsNow_thenBookingsFound() {
        int expectedBookings = 1;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        booking1.setStart(now);
        booking1.setEnd(now.plusDays(1));
        booking2.setStart(now.plusSeconds(1));
        booking2.setEnd(now.plusDays(1));

        List<Booking> bookings = bookingRepository.findLastByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
        assertTrue(bookings.contains(booking1));
        assertFalse(bookings.contains(booking2));
    }

    @Test
    void findLastByItemIds_whenNoPriorBooking_thenEmptyListReturned() {
        int expectedBookings = 0;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findLastByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findLastByItemIds_whenBookings_thenEmptyListReturned() {
        int expectedBookings = 0;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item2.getId(), item3.getId()));
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.delete(booking3);

        List<Booking> bookings = bookingRepository.findLastByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findNextByItemId_whenFound_thenBookingReturned() {
        Long itemId = item1.getId();
        LocalDateTime now = LocalDateTime.now();

        Booking foundBooking = bookingRepository.findNextByItemId(itemId, now);

        assertEquals(booking1, foundBooking);
    }

    @Test
    void findNextByItemId_whenStartIsNow_thenBookingNotFound() {
        Long itemId = item2.getId();
        LocalDateTime now = LocalDateTime.now();
        booking3.setStart(now);

        Booking foundBooking = bookingRepository.findNextByItemId(itemId, now);

        assertNull(foundBooking);
    }

    @Test
    void findNextByItemId_whenBookingsOnlyInPast_thenBookingNotFound() {
        Long itemId = item2.getId();
        LocalDateTime now = LocalDateTime.now();
        booking3.setStart(now.minusDays(3));
        booking3.setEnd(now.minusDays(1));

        Booking foundBooking = bookingRepository.findNextByItemId(itemId, now);

        assertNull(foundBooking);
    }

    @Test
    void findNextByItemId_whenNoBookings_thenBookingNotFound() {
        Long itemId = item3.getId();
        LocalDateTime now = LocalDateTime.now();

        Booking foundBooking = bookingRepository.findNextByItemId(itemId, now);

        assertNull(foundBooking);
    }

    @Test
    void findNextByItemIds_whenFound_thenBookingsListReturned() {
        int expectedBookings = 2;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findNextByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
        assertTrue(bookings.contains(booking1));
        assertTrue(bookings.contains(booking3));
    }


    @Test
    void findNextByItemIds_whenStartIsNow_thenBookingsNotReturned() {
        int expectedBookings = 1;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        // now
        booking1.setStart(now);
        booking1.setEnd(now.plusDays(1));

        // future
        booking3.setStart(now.plusHours(1));
        booking3.setEnd(now.plusDays(1));

        // past
        booking4.setStart(now.minusDays(8));
        booking4.setEnd(now.minusDays(7));
        booking2.setStart(now.minusDays(4));
        booking2.setEnd(now.minusDays(3));

        List<Booking> bookings = bookingRepository.findNextByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
        assertTrue(bookings.contains(booking3));
        assertFalse(bookings.contains(booking1));
    }

    @Test
    void findNextByItemIds_whenNoFutureBookings_thenEmptyListReturned() {
        int expectedBookings = 0;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        LocalDateTime now = LocalDateTime.now();

        booking4.setStart(now.minusDays(8));
        booking4.setEnd(now.minusDays(7));
        booking3.setStart(now.minusDays(6));
        booking3.setEnd(now.minusDays(5));
        booking2.setStart(now.minusDays(4));
        booking2.setEnd(now.minusDays(3));
        booking1.setStart(now.minusDays(2));
        booking1.setEnd(now.minusDays(1));

        List<Booking> bookings = bookingRepository.findNextByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
    }


    @Test
    void findNextByItemIds_whenNoBookings_thenEmptyListReturned() {
        int expectedBookings = 0;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item2.getId(), item3.getId()));
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.delete(booking3);

        List<Booking> bookings = bookingRepository.findNextByItemIds(itemIds, now);

        assertEquals(expectedBookings, bookings.size());
    }


    @Test
    void findOverlaps_whenSameStart_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart();
        LocalDateTime ends = LocalDateTime.now().plusDays(2);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenSameEnd_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = LocalDateTime.now().plusHours(3);
        LocalDateTime ends = booking1.getEnd();

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenEndBetweenStartAndEnd_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().minusDays(1);
        LocalDateTime ends = booking1.getEnd().minusDays(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenStartBetweenStartAndEnd_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().plusDays(1);
        LocalDateTime ends = booking1.getEnd().plusDays(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenStartBeforeStartAndEndAfterEnd_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().minusHours(1);
        LocalDateTime ends = booking1.getEnd().plusHours(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenBetweenStartAndEnd_thenBookingReturned() {
        int expectedBookings = 1;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().plusHours(1);
        LocalDateTime ends = booking1.getEnd().minusHours(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findOverlaps_whenMultipleOverlaps_ReturnAll() {
        int expectedBookings = 2;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().minusHours(1);
        LocalDateTime ends = booking2.getEnd().minusHours(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
        assertTrue(bookings.contains(booking1));
        assertTrue(bookings.contains(booking2));
    }

    @Test
    void findOverlaps_whenNoOverlaps_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart().minusHours(2);
        LocalDateTime ends = booking1.getStart().minusHours(1);

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
    }

    @Test
    void findOverlaps_whenNoBookings_thenEmptyListReturned() {
        int expectedBookings = 0;
        Long itemId = booking1.getItem().getId();
        LocalDateTime starts = booking1.getStart();
        LocalDateTime ends = booking1.getEnd();

        bookingRepository.deleteAll();

        List<Booking> bookings = bookingRepository.findOverlaps(itemId, starts, ends);

        assertEquals(expectedBookings, bookings.size());
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
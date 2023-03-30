package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.search.booker.BookingSearchByBooker;
import ru.practicum.shareit.booking.search.owner.BookingSearchByOwner;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@SpringBootTest
class BookingServiceImplSBTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    List<BookingSearchByOwner> searchByOwnerQueries;

    @Autowired
    List<BookingSearchByBooker> searchByBookerQueries;

    private static final String DEFAULT_STATE = "all";

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10,
            Sort.by(Sort.Direction.DESC, "start"));


    @Test
    void getAllByBookerId_whenValid_thenDtosReturned() {
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();
        int expectedCount = 3;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker(isA(User.class), isA(Pageable.class)))
                .thenReturn(bookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, DEFAULT_STATE, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenStateIsFuture_thenDtosWithFutureBookingsReturned() {
        String state = "future";
        int expectedCount = 2;

        List<Booking> futureBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        bookings.get(2).setStart(LocalDateTime.now().minusDays(3));
        bookings.get(2).setEnd(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isFuture(booking)) futureBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerFuture(isA(User.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(futureBookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isAfter(LocalDateTime.now()));
        assertTrue(outputDtos.get(1).getStart().isAfter(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenStateIsCurrent_thenDtosWithCurrentBookingsReturned() {
        String state = "current";
        int expectedCount = 1;

        List<Booking> currentBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        bookings.get(0).setStart(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isCurrent(booking)) currentBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerCurrent(isA(User.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(currentBookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(outputDtos.get(0).getEnd().isAfter(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenStateIsPast_thenDtosWithPastBookingsReturned() {
        String state = "past";
        int expectedCount = 1;

        List<Booking> pastBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        bookings.get(0).setStart(LocalDateTime.now().minusDays(3));
        bookings.get(0).setEnd(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isPast(booking)) pastBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerPast(isA(User.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(pastBookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(outputDtos.get(0).getEnd().isBefore(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenStateIsWaiting_thenDtosWithWaitingStateReturned() {
        String state = "waiting";
        int expectedCount = 1;

        List<Booking> bookingsWaiting = new ArrayList<>();
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        bookings.get(0).setStatus(BookingState.WAITING);

        bookings.forEach(booking -> {
            if (isWaiting(booking)) bookingsWaiting.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerWaiting(isA(User.class), isA(Pageable.class)))
                .thenReturn(bookingsWaiting);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertEquals(BookingState.WAITING, bookings.get(0).getStatus());
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenStateIsRejected_thenDtosWithRejectedStateReturned() {
        String state = "rejected";
        int expectedCount = 1;

        List<Booking> rejectedBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        bookings.get(0).setStatus(BookingState.REJECTED);

        bookings.forEach(booking -> {
            if (isRejected(booking)) rejectedBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerRejected(isA(User.class), isA(Pageable.class)))
                .thenReturn(rejectedBookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertEquals(BookingState.REJECTED, bookings.get(0).getStatus());
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByBookerId_whenNoBookings_thenEmptyListReturned() {
        int expectedCount = 0;
        User booker = createBooker();
        Long bookerId = booker.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker(isA(User.class), isA(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, DEFAULT_STATE, DEFAULT_PAGEABLE);
        assertEquals(expectedCount, outputDtos.size());
    }

    @Test
    void getAllByOwnerId_whenNoBookings_thenEmptyListReturned() {
        int expectedCount = 0;
        User owner = createOwner();
        Long ownerId = owner.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerId(isA(Long.class), isA(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, DEFAULT_STATE, DEFAULT_PAGEABLE);
        assertEquals(expectedCount, outputDtos.size());
    }

    @Test
    void getAllByOwnerId_whenValid_thenDtosReturned() {
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();
        int expectedCount = 2;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerId(isA(Long.class), isA(Pageable.class)))
                .thenReturn(bookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, DEFAULT_STATE, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByOwnerId_whenStateIsFuture_thenFutureBookingDtosReturned() {
        String state = "future";
        int expectedCount = 1;

        List<Booking> futureBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();

        bookings.get(1).setStart(LocalDateTime.now().minusDays(3));
        bookings.get(1).setEnd(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isFuture(booking)) futureBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdFuture(isA(Long.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(futureBookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isAfter(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByOwnerId_whenStateIsCurrent_thenCurrentBookingDtosReturned() {
        String state = "current";
        int expectedCount = 1;

        List<Booking> currentBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();

        bookings.get(0).setStart(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isCurrent(booking)) currentBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdCurrent(isA(Long.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(currentBookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(outputDtos.get(0).getEnd().isAfter(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByOwnerId_whenStateIsPast_thenPastBookingDtosReturned() {
        String state = "past";
        int expectedCount = 1;

        List<Booking> pastBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();

        bookings.get(0).setStart(LocalDateTime.now().minusDays(3));
        bookings.get(0).setEnd(LocalDateTime.now().minusDays(1));

        bookings.forEach(booking -> {
            if (isPast(booking)) pastBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdPast(isA(Long.class), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(pastBookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertTrue(outputDtos.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(outputDtos.get(0).getEnd().isBefore(LocalDateTime.now()));
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByOwnerId_whenStateIsWaiting_thenDtosWithWaitingStatusReturned() {
        String state = "waiting";
        int expectedCount = 1;

        List<Booking> waitingBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();

        bookings.get(0).setStatus(BookingState.WAITING);

        bookings.forEach(booking -> {
            if (isWaiting(booking)) waitingBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdWaiting(isA(Long.class), isA(Pageable.class)))
                .thenReturn(waitingBookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertEquals(BookingState.WAITING, outputDtos.get(0).getStatus());
        checkFields(bookings, outputDtos);
    }

    @Test
    void getAllByOwnerId_whenStateIsRejected_thenDtosWithRejectedStatusReturned() {
        String state = "rejected";
        int expectedCount = 1;

        List<Booking> rejectedBookings = new ArrayList<>();
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();

        bookings.get(0).setStatus(BookingState.REJECTED);

        bookings.forEach(booking -> {
            if (isRejected(booking)) rejectedBookings.add(booking);
        });

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdRejected(isA(Long.class), isA(Pageable.class)))
                .thenReturn(rejectedBookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertNotEquals(bookings.size(), outputDtos.size());
        assertEquals(BookingState.REJECTED, outputDtos.get(0).getStatus());
        checkFields(bookings, outputDtos);
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private void checkFields(List<Booking> bookings, List<BookingDto> bookingDtos) {
        for (int i = 0; i < bookingDtos.size(); i++) {
            assertEquals(bookings.get(i).getId(), bookingDtos.get(i).getId());
            assertEquals(bookings.get(i).getStart(), bookingDtos.get(i).getStart());
            assertEquals(bookings.get(i).getEnd(), bookingDtos.get(i).getEnd());
            assertEquals(bookings.get(i).getStatus(), bookingDtos.get(i).getStatus());
            assertEquals(bookings.get(i).getBooker().getId(), bookingDtos.get(i).getBooker().getId());
            assertEquals(bookings.get(i).getItem().getId(), bookingDtos.get(i).getItem().getId());
            assertEquals(bookings.get(i).getItem().getName(), bookingDtos.get(i).getItem().getName());
        }
    }

    private Booking createBooking(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }

    private Item createItem(Long id, String name, String description, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private Item createItem() {
        return createItem(
                1L,
                "Peter's Item 1",
                "Peter's Item 1 Description",
                createOwner());
    }

    private User createOwner() {
        return createUser(1L, "Peter", "peter@example.com");
    }

    private User createBooker() {
        return createUser(2L, "Kate", "kate@example.com");
    }

    private User createOtherUser() {
        return createUser(3L, "Paul", "paul@example.com");
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private List<Booking> createBookingsForBooker() {
        User owner1 = createOwner();
        User owner2 = createOtherUser();
        User booker = createBooker();

        Item item1 = createItem();
        Item item2 = createItem(2L, "Paul's Item 1",
                "Paul's Item 1 Description", owner2);
        Item item3 = createItem(3L, "Peter's Item 2",
                "Peter's Item 2 Description", owner1);

        Booking booking1 = createBooking(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                item1, booker);
        Booking booking2 = createBooking(2L,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7),
                item2, booker);
        Booking booking3 = createBooking(3L,
                LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(11),
                item3, booker);

        return new ArrayList<>(Arrays.asList(booking1, booking2, booking3));
    }

    private List<Booking> createBookingsForOwner() {
        User booker1 = createBooker();
        User booker2 = createOtherUser();

        Item item = createItem();

        Booking booking1 = createBooking(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                item, booker1);
        Booking booking2 = createBooking(2L,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7),
                item, booker2);

        return new ArrayList<>(Arrays.asList(booking1, booking2));
    }

    private boolean isFuture(Booking booking) {
        return booking.getStart().isAfter(LocalDateTime.now());
    }

    private boolean isPast(Booking booking) {
        return booking.getStart().isBefore(LocalDateTime.now()) &&
                booking.getEnd().isBefore(LocalDateTime.now());
    }

    private boolean isCurrent(Booking booking) {
        return booking.getStart().isBefore(LocalDateTime.now()) &&
                !booking.getEnd().isBefore(LocalDateTime.now());
    }

    private boolean isWaiting(Booking booking) {
        return BookingState.WAITING.equals(booking.getStatus());
    }

    private boolean isRejected(Booking booking) {
        return BookingState.REJECTED.equals(booking.getStatus());
    }
}
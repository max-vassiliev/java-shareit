package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.common.exception.BookingStatusException;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.common.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Spy
    private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    private static final String DEFAULT_STATE = "all";

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10,
            Sort.by(Sort.Direction.DESC, "start"));


    @Test
    void create_whenValid_thenDtoReturned() {
        BookingDto inputDto = createBookingDto();
        User booker = createBooker();
        Item item = createItem();
        Long bookingId = 1L;
        BookingState status = BookingState.WAITING;
        Booking booking = createBooking(bookingId, inputDto, item, booker, status);

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(isA(Booking.class)))
                .thenReturn(booking);

        BookingDto outputDto = bookingService.create(inputDto);

        assertEquals(bookingId, outputDto.getId());
        assertEquals(inputDto.getStart(), outputDto.getStart());
        assertEquals(inputDto.getEnd(), outputDto.getEnd());
        assertEquals(status, outputDto.getStatus());
        assertEquals(inputDto.getBookerId(), outputDto.getBooker().getId());
        assertEquals(inputDto.getItemId(), outputDto.getItem().getId());
    }

    @Test
    void create_whenItemUnavailable_thenValidationExceptionThrown() {
        BookingDto inputDto = createBookingDto();
        User booker = createBooker();
        Item item = createItem();
        item.setIsAvailable(false);

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(inputDto));
        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void create_whenBookerNotFound_thenEntityNotFoundExceptionThrown() {
        BookingDto inputDto = createBookingDto();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.create(inputDto));
    }

    @Test
    void create_whenItemNotFound_thenEntityNotFoundExceptionThrown() {
        BookingDto inputDto = createBookingDto();
        User booker = createBooker();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.create(inputDto));
    }

    @Test
    void create_whenBookerIsOwner_thenEntityNotFoundExceptionThrown() {
        BookingDto inputDto = createBookingDto();
        User booker = createOwner();
        Item item = createItem();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.create(inputDto));
    }

    @Test
    void create_whenEndIsBeforeStart_thenValidationExceptionThrown() {
        BookingDto inputDto = createBookingDto(1L,
                2L,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1));
        User booker = createBooker();
        Item item = createItem();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.create(inputDto));
    }

    @Test
    void create_whenOverlap_thenConflictExceptionThrown() {
        Booking existingBooking = createBooking();
        existingBooking.setStatus(BookingState.APPROVED);

        User booker = createBooker();
        Item item = existingBooking.getItem();

        BookingDto inputDto = createBookingDto(
                existingBooking.getItem().getId(),
                booker.getId(),
                existingBooking.getStart().plusHours(1),
                existingBooking.getEnd().plusHours(1));

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findOverlaps(isA(Long.class), isA(LocalDateTime.class), isA(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(existingBooking));

        assertThrows(ConflictException.class,
                () -> bookingService.create(inputDto));
    }

    @Test
    void approve_whenValidAndApproved_thenDtoWithApprovedStatusReturned() {
        Boolean approved = true;
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        Long ownerId = booking.getItem().getOwner().getId();
        User owner = booking.getItem().getOwner();

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));

        BookingDto outputDto = bookingService.approve(bookingId, ownerId, approved);

        assertEquals(bookingId, outputDto.getId());
        assertEquals(BookingState.APPROVED, outputDto.getStatus());
        assertEquals(booking.getStart(), outputDto.getStart());
        assertEquals(booking.getEnd(), outputDto.getEnd());
        assertEquals(booking.getBooker().getId(), outputDto.getBooker().getId());
        assertEquals(booking.getItem().getId(), outputDto.getItem().getId());
        assertEquals(booking.getItem().getName(), outputDto.getItem().getName());
    }

    @Test
    void approve_whenValidAndRejected_thenDtoWithRejectedStatusReturned() {
        Boolean approved = false;
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        Long ownerId = booking.getItem().getOwner().getId();
        User owner = booking.getItem().getOwner();

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));

        BookingDto outputDto = bookingService.approve(bookingId, ownerId, approved);

        assertEquals(bookingId, outputDto.getId());
        assertEquals(BookingState.REJECTED, outputDto.getStatus());
        assertEquals(booking.getStart(), outputDto.getStart());
        assertEquals(booking.getEnd(), outputDto.getEnd());
        assertEquals(booking.getBooker().getId(), outputDto.getBooker().getId());
        assertEquals(booking.getItem().getId(), outputDto.getItem().getId());
        assertEquals(booking.getItem().getName(), outputDto.getItem().getName());
    }

    @Test
    void approve_whenUserNotFound_thenEntityNotFoundException() {
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        User falseOwner = createOtherUser();
        Long falseOwnerId = falseOwner.getId();
        Boolean approved = true;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(falseOwner));

        assertThrows(ValidationException.class,
                () -> bookingService.approve(bookingId, falseOwnerId, approved));
    }

    @Test
    void approve_whenUserNotOwner_thenValidationExceptionThrown() {
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        User falseOwner = createOtherUser();
        Long falseOwnerId = falseOwner.getId();
        Boolean approved = true;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(falseOwner));

        assertThrows(ValidationException.class,
                () -> bookingService.approve(bookingId, falseOwnerId, approved));
    }

    @Test
    void approve_whenApprovedByBooker_thenEntityNotFoundExceptionThrown() {
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        User booker = createBooker();
        Long bookerId = booker.getId();
        Boolean approved = true;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approve(bookingId, bookerId, approved));
    }

    @Test
    void approve_whenBookingAlreadyApproved_thenValidationExceptionThrown() {
        Booking booking = createBooking();
        booking.setStatus(BookingState.APPROVED);
        Long bookingId = booking.getId();
        User owner = booking.getItem().getOwner();
        Long ownerId = owner.getId();
        Boolean approved = true;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.approve(bookingId, ownerId, approved));
    }

    @Test
    void approve_whenBookingAlreadyRejected_thenValidationExceptionThrown() {
        Booking booking = createBooking();
        booking.setStatus(BookingState.REJECTED);
        Long bookingId = booking.getId();
        User owner = booking.getItem().getOwner();
        Long ownerId = owner.getId();
        Boolean approved = true;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.approve(bookingId, ownerId, approved));
    }

    @Test
    void getById_whenInvokedByBooker_thenDtoReturned() {
        Booking booking = createBooking();
        User booker = booking.getBooker();
        Long bookingId = booking.getId();
        Long bookerId = booking.getBooker().getId();

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));

        BookingDto outputDto = bookingService.getById(bookingId, bookerId);
        assertEquals(bookingId, outputDto.getId());
        assertEquals(booking.getStart(), outputDto.getStart());
        assertEquals(booking.getEnd(), outputDto.getEnd());
        assertEquals(booking.getStatus(), outputDto.getStatus());
        assertEquals(bookerId, outputDto.getBooker().getId());
        assertEquals(booking.getItem().getId(), outputDto.getItem().getId());
        assertEquals(booking.getItem().getName(), outputDto.getItem().getName());
    }

    @Test
    void getById_whenInvokedByOwner_thenDtoReturned() {
        Booking booking = createBooking();
        User owner = booking.getItem().getOwner();
        Long bookingId = booking.getId();
        Long ownerId = owner.getId();

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));

        BookingDto outputDto = bookingService.getById(bookingId, ownerId);

        assertEquals(bookingId, outputDto.getId());
        assertEquals(booking.getStart(), outputDto.getStart());
        assertEquals(booking.getEnd(), outputDto.getEnd());
        assertEquals(booking.getStatus(), outputDto.getStatus());
        assertEquals(booking.getBooker().getId(), outputDto.getBooker().getId());
        assertEquals(booking.getItem().getId(), outputDto.getItem().getId());
        assertEquals(booking.getItem().getName(), outputDto.getItem().getName());
    }

    @Test
    void getById_whenBookingNotFound_thenEntityNotFoundExceptionThrown() {
        Long bookingId = 1000L;
        Long ownerId = 1L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getById(bookingId, ownerId));
    }

    @Test
    void getById_whenUserNotFound_thenEntityNotFoundExceptionThrown() {
        Booking booking = createBooking();
        Long bookingId = booking.getId();
        Long userId = 3L;

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getById(bookingId, userId));
    }

    @Test
    void getById_whenUserNotBookerOrOwner_thenEntityNotFoundExceptionThrown() {
        Booking booking = createBooking();
        User user = createOtherUser();
        Long bookingId = booking.getId();
        Long userId = user.getId();

        when(bookingRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getById(bookingId, userId));
    }

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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
        assertEquals(bookings.get(1).getId(), outputDtos.get(1).getId());
        assertEquals(bookings.get(1).getStart(), outputDtos.get(1).getStart());
        assertEquals(bookings.get(1).getEnd(), outputDtos.get(1).getEnd());
        assertEquals(bookings.get(1).getStatus(), outputDtos.get(1).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), outputDtos.get(1).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), outputDtos.get(1).getItem().getId());
        assertEquals(bookings.get(1).getItem().getName(), outputDtos.get(1).getItem().getName());
        assertEquals(bookings.get(2).getId(), outputDtos.get(2).getId());
        assertEquals(bookings.get(2).getStart(), outputDtos.get(2).getStart());
        assertEquals(bookings.get(2).getEnd(), outputDtos.get(2).getEnd());
        assertEquals(bookings.get(2).getStatus(), outputDtos.get(2).getStatus());
        assertEquals(bookings.get(2).getBooker().getId(), outputDtos.get(2).getBooker().getId());
        assertEquals(bookings.get(2).getItem().getId(), outputDtos.get(2).getItem().getId());
        assertEquals(bookings.get(2).getItem().getName(), outputDtos.get(2).getItem().getName());
    }

    @Test
    void getAllByBookerId_whenStateIsAll_thenAllDtosReturned() {
        String state = "all";
        int expectedCount = 3;

        List<Booking> bookings = createBookingsForBooker();
        User booker = bookings.get(0).getBooker();
        Long bookerId = booker.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker(isA(User.class), isA(Pageable.class)))
                .thenReturn(bookings);

        List<BookingDto> outputDtos = bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
        assertEquals(bookings.get(1).getId(), outputDtos.get(1).getId());
        assertEquals(bookings.get(1).getStart(), outputDtos.get(1).getStart());
        assertEquals(bookings.get(1).getEnd(), outputDtos.get(1).getEnd());
        assertEquals(bookings.get(1).getStatus(), outputDtos.get(1).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), outputDtos.get(1).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), outputDtos.get(1).getItem().getId());
        assertEquals(bookings.get(1).getItem().getName(), outputDtos.get(1).getItem().getName());
        assertEquals(bookings.get(2).getId(), outputDtos.get(2).getId());
        assertEquals(bookings.get(2).getStart(), outputDtos.get(2).getStart());
        assertEquals(bookings.get(2).getEnd(), outputDtos.get(2).getEnd());
        assertEquals(bookings.get(2).getStatus(), outputDtos.get(2).getStatus());
        assertEquals(bookings.get(2).getBooker().getId(), outputDtos.get(2).getBooker().getId());
        assertEquals(bookings.get(2).getItem().getId(), outputDtos.get(2).getItem().getId());
        assertEquals(bookings.get(2).getItem().getName(), outputDtos.get(2).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
        assertEquals(bookings.get(1).getId(), outputDtos.get(1).getId());
        assertEquals(bookings.get(1).getStart(), outputDtos.get(1).getStart());
        assertEquals(bookings.get(1).getEnd(), outputDtos.get(1).getEnd());
        assertEquals(bookings.get(1).getStatus(), outputDtos.get(1).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), outputDtos.get(1).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), outputDtos.get(1).getItem().getId());
        assertEquals(bookings.get(1).getItem().getName(), outputDtos.get(1).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
    void getAllByBookerId_whenBookerNotFound_thenEntityNotFoundExceptionThrown() {
        Long bookerId = 1000L;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getAllByBookerId(bookerId, DEFAULT_STATE, DEFAULT_PAGEABLE));
    }

    @Test
    void getAllByBookerId_whenStateUnknown_thenBookingStatusExceptionThrown() {
        String state = "unknown";
        User booker = createBooker();
        Long bookerId = booker.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(booker));

        assertThrows(BookingStatusException.class,
                () -> bookingService.getAllByBookerId(bookerId, state, DEFAULT_PAGEABLE));
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
        assertEquals(bookings.get(1).getId(), outputDtos.get(1).getId());
        assertEquals(bookings.get(1).getStart(), outputDtos.get(1).getStart());
        assertEquals(bookings.get(1).getEnd(), outputDtos.get(1).getEnd());
        assertEquals(bookings.get(1).getStatus(), outputDtos.get(1).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), outputDtos.get(1).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), outputDtos.get(1).getItem().getId());
        assertEquals(bookings.get(1).getItem().getName(), outputDtos.get(1).getItem().getName());
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
    void getAllByOwnerId_whenStateIsAll_thenAllDtosReturned() {
        String state = "all";
        List<Booking> bookings = createBookingsForOwner();
        User owner = bookings.get(0).getItem().getOwner();
        Long ownerId = owner.getId();
        int expectedCount = 2;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerId(isA(Long.class), isA(Pageable.class)))
                .thenReturn(bookings);

        List<BookingDto> outputDtos = bookingService.getAllByOwnerId(ownerId, state, DEFAULT_PAGEABLE);

        assertEquals(expectedCount, outputDtos.size());
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
        assertEquals(bookings.get(1).getId(), outputDtos.get(1).getId());
        assertEquals(bookings.get(1).getStart(), outputDtos.get(1).getStart());
        assertEquals(bookings.get(1).getEnd(), outputDtos.get(1).getEnd());
        assertEquals(bookings.get(1).getStatus(), outputDtos.get(1).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), outputDtos.get(1).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), outputDtos.get(1).getItem().getId());
        assertEquals(bookings.get(1).getItem().getName(), outputDtos.get(1).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
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
        assertEquals(bookings.get(0).getId(), outputDtos.get(0).getId());
        assertEquals(bookings.get(0).getStart(), outputDtos.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), outputDtos.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), outputDtos.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), outputDtos.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), outputDtos.get(0).getItem().getId());
        assertEquals(bookings.get(0).getItem().getName(), outputDtos.get(0).getItem().getName());
    }

    @Test
    void getAllByOwnerId_whenOwnerNotFound_thenEntityNotFoundExceptionThrown() {
        Long ownerId = 1000L;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getAllByBookerId(ownerId, DEFAULT_STATE, DEFAULT_PAGEABLE));
    }

    @Test
    void getAllByOwnerId_whenStateUnknown_thenBookingStatusExceptionThrown() {
        String state = "unknown";
        User owner = createOwner();
        Long ownerId = owner.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(owner));

        assertThrows(BookingStatusException.class,
                () -> bookingService.getAllByBookerId(ownerId, state, DEFAULT_PAGEABLE));
    }


    // ----------
    // Шаблоны
    // ----------

    private Booking createBooking(Long id, BookingDto bookingDto, Item item, User booker, BookingState status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
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

    private Booking createBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(3));
        booking.setEnd(LocalDateTime.now().plusDays(5));
        booking.setItem(createItem());
        booking.setBooker(createBooker());
        booking.setStatus(BookingState.WAITING);
        return booking;
    }

    private BookingDto createBookingDto(Long itemId, Long bookerId, LocalDateTime start, LocalDateTime end) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setBookerId(bookerId);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    private BookingDto createBookingDto() {
        return createBookingDto(
                1L,
                2L,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5)
        );
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
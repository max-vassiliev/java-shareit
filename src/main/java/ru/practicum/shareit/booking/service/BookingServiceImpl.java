package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.common.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = getBooking(bookingId);
        getUser(userId);

        if (isUserAuthorized(userId, booking)) {
            return bookingMapper.toBookingDto(booking);
        } else {
            throw new EntityNotFoundException("Бронирование не найдено",
                    Booking.class);
        }
    }

    @Override
    public List<BookingDto> getAllByBookerId(Long bookerId, String state, Pageable pageable) {
        User booker = getUser(bookerId);
        BookingStateDto stateDto = BookingStateDto.fromString(state);
        List<Booking> foundBookings = findByBookerAndState(booker, stateDto, pageable);
        if (foundBookings.isEmpty()) return Collections.emptyList();

        return foundBookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwnerId(Long ownerId, String state, Pageable pageable) {
        getUser(ownerId);
        BookingStateDto stateDto = BookingStateDto.fromString(state);

        List<Booking> bookings = findByOwnerIdAndState(ownerId, stateDto, pageable);
        if (bookings.isEmpty()) return Collections.emptyList();

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDto create(BookingDto bookingDto) {
        User booker = getUser(bookingDto.getBookerId());
        Item item = getItem(bookingDto.getItemId());
        validateBeforeCreate(item, bookingDto, booker);

        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = getBooking(bookingId);
        validateBeforeApprove(ownerId, booking);

        if (approved) {
            booking.setStatus(BookingState.APPROVED);
        } else {
            booking.setStatus(BookingState.REJECTED);
        }

        return bookingMapper.toBookingDto(booking);
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найдено бронирование с ID " + bookingId, Booking.class
                ));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + userId, User.class
                ));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найдена вещь с ID " + itemId, Item.class
                ));
    }

    List<Booking> findByBookerAndState(User booker, BookingStateDto stateDto, Pageable pageable) {
        List<Booking> foundBookings;

        switch (stateDto) {
            case PAST:
                foundBookings = bookingRepository.findAllByBookerPast(booker, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                foundBookings = bookingRepository.findAllByBookerCurrent(booker, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                foundBookings = bookingRepository.findAllByBookerFuture(booker, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                foundBookings = bookingRepository.findAllByBookerWaiting(booker, pageable);
                break;
            case REJECTED:
                foundBookings = bookingRepository.findAllByBookerRejected(booker, pageable);
                break;
            default:
                foundBookings = bookingRepository.findAllByBooker(booker, pageable);
        }

        return foundBookings;
    }

    private List<Booking> findByOwnerIdAndState(Long ownerId, BookingStateDto stateDto, Pageable pageable) {
        List<Booking> foundBookings;

        switch (stateDto) {
            case PAST:
                foundBookings = bookingRepository.findByOwnerIdPast(ownerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                foundBookings = bookingRepository.findByOwnerIdCurrent(ownerId, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                foundBookings = bookingRepository.findByOwnerIdFuture(ownerId, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                foundBookings = bookingRepository.findByOwnerIdWaiting(ownerId, pageable);
                break;
            case REJECTED:
                foundBookings = bookingRepository.findByOwnerIdRejected(ownerId, pageable);
                break;
            default:
                foundBookings = bookingRepository.findByOwnerId(ownerId, pageable);
        }

        return foundBookings;
    }

    private void validateBeforeCreate(Item item, BookingDto bookingDto, User booker) {
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Дата завершения аренды должна быть позже даты начала аренды");
        }

        if (Objects.equals(booker.getId(), item.getOwner().getId())) {
            throw new EntityNotFoundException("Забронировать свою вещь не получится", Booking.class);
        }

        if (!item.getIsAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        List<Booking> overlaps = bookingRepository.findOverlaps(item.getId(),
                bookingDto.getStart(), bookingDto.getEnd());

        if (!overlaps.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Пересечение по времени с другими бронированиями: ");
            overlaps.forEach(overlap -> messageBuilder.append(overlap.getStart()).append(" — ")
                    .append(overlap.getEnd()).append("; "));
            String errorMessage = messageBuilder.toString();

            throw new ConflictException(errorMessage);
        }
    }

    private void validateBeforeApprove(Long ownerId, Booking booking) {
        if (BookingState.APPROVED.equals(booking.getStatus()) || BookingState.REJECTED.equals(booking.getStatus())) {
            throw new ValidationException("Менять статус этого бронирования уже нельзя");
        }
        getUser(ownerId);
        if (Objects.equals(ownerId, booking.getBooker().getId())) {
            throw new EntityNotFoundException("Подтверждать бронирование может только владелец вещи", Booking.class);
        }
        if (!Objects.equals(ownerId, booking.getItem().getOwner().getId())) {
            throw new ValidationException("Подтверждать бронирование может только владелец вещи");
        }
    }

    private boolean isUserAuthorized(Long userId, Booking booking) {
        return Objects.equals(userId, booking.getBooker().getId()) ||
                Objects.equals(userId, booking.getItem().getOwner().getId());
    }
}

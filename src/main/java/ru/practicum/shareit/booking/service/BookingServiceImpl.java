package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.error.exception.ValidationException;
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


    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = getBooking(bookingId);
        getUser(userId);

        if (isUserAuthorized(userId, booking)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new EntityNotFoundException("Бронирование не найдено",
                    Booking.class);
        }
    }

    @Override
    public List<BookingDto> getAllByBookerId(Long bookerId, String state) {
        User booker = getUser(bookerId);
        BookingStateDto stateDto = BookingStateDto.fromString(state);
        List<Booking> foundBookings = findByBookerAndState(booker, stateDto);
        if (foundBookings.isEmpty()) return Collections.emptyList();

        return foundBookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwnerId(Long ownerId, String state) {
        getUser(ownerId);
        List<Item> ownerItems = getItemsByOwnerId(ownerId);
        BookingStateDto stateDto = BookingStateDto.fromString(state);

        List<Booking> foundBookings = findByOwnerItemsAndState(ownerItems, stateDto);
        if (foundBookings.isEmpty()) return Collections.emptyList();

        return foundBookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDto create(BookingDto bookingDto) {
        User booker = getUser(bookingDto.getBookerId());
        Item item = getItem(bookingDto.getItemId());
        validateBeforeCreate(item, bookingDto, booker);
        Booking booking = BookingMapper.toBookingCreate(bookingDto, booker, item);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
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

        return BookingMapper.toBookingDto(booking);
    }


    // ----------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ----------------------

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

    private List<Item> getItemsByOwnerId(Long ownerId) {
        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        if (ownerItems.isEmpty()) {
            throw new ValidationException("У пользователя с ID " + ownerId + " пока нет вещей для бронирования");
        }
        return ownerItems;
    }

    List<Booking> findByBookerAndState(User booker, BookingStateDto stateDto) {
        List<Booking> foundBookings;

        switch (stateDto) {
            case PAST:
                foundBookings = bookingRepository.findAllByBookerPast(booker,LocalDateTime.now());
                break;
            case CURRENT:
                foundBookings = bookingRepository.findAllByBookerCurrent(booker, LocalDateTime.now());
                break;
            case FUTURE:
                foundBookings = bookingRepository.findAllByBookerFuture(booker, LocalDateTime.now());
                break;
            case WAITING:
                foundBookings = bookingRepository.findAllByBookerWaiting(booker);
                break;
            case REJECTED:
                foundBookings = bookingRepository.findAllByBookerRejected(booker);
                break;
            default:
                foundBookings = bookingRepository.findAllByBooker(booker);
        }

        return foundBookings;
    }


    private List<Booking> findByOwnerItemsAndState(List<Item> ownerItems, BookingStateDto stateDto) {
        List<Booking> foundBookings;

        switch (stateDto) {
            case PAST:
                foundBookings = bookingRepository.findAllByOwnerItemsPast(ownerItems, LocalDateTime.now());
                break;
            case CURRENT:
                foundBookings = bookingRepository.findAllByOwnerItemsCurrent(ownerItems, LocalDateTime.now());
                break;
            case FUTURE:
                foundBookings = bookingRepository.findAllByOwnerItemsFuture(ownerItems, LocalDateTime.now());
                break;
            case WAITING:
                foundBookings = bookingRepository.findAllByOwnerItemsWaiting(ownerItems);
                break;
            case REJECTED:
                foundBookings = bookingRepository.findAllByOwnerItemsRejected(ownerItems);
                break;
            default:
                foundBookings = bookingRepository.findAllByOwnerItems(ownerItems);
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
        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + ownerId, User.class
                ));
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

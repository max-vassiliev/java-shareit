package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto bookingDto);

    BookingDto approve(Long bookingId, Long ownerId, Boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllByBookerId(Long bookerId, String state);

    List<BookingDto> getAllByOwnerId(Long ownerId, String state);
}

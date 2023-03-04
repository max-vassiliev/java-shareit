package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto bookingDto);

    BookingDto approve(Long bookingId, Long ownerId, Boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllByBookerId(Long bookerId, String state, Pageable pageable);

    List<BookingDto> getAllByOwnerId(Long ownerId, String state, Pageable pageable);
}

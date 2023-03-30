package ru.practicum.shareit.booking.search.owner;

import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.search.owner.params.BookingSearchByOwnerParams;

import java.util.List;

public interface BookingSearchByOwner {

    List<Booking> search(BookingSearchByOwnerParams params);

    BookingStateDto getType();
}

package ru.practicum.shareit.booking.search.booker;

import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.search.booker.params.BookingSearchByBookerParams;

import java.util.List;

public interface BookingSearchByBooker {

    List<Booking> search(BookingSearchByBookerParams params);

    BookingStateDto getType();

}

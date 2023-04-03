package ru.practicum.shareit.booking.search.booker;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.search.booker.params.BookingSearchByBookerParams;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingSearchByBookerFuture implements BookingSearchByBooker {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> search(BookingSearchByBookerParams params) {
        return bookingRepository.findAllByBookerFuture(params.getBooker(), LocalDateTime.now(), params.getPageable());
    }

    @Override
    public BookingStateDto getType() {
        return BookingStateDto.FUTURE;
    }
}

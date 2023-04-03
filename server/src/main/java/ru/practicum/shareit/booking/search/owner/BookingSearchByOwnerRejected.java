package ru.practicum.shareit.booking.search.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.search.owner.params.BookingSearchByOwnerParams;

import java.util.List;


@Component
@RequiredArgsConstructor
public class BookingSearchByOwnerRejected implements BookingSearchByOwner {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> search(BookingSearchByOwnerParams params) {
        return bookingRepository.findByOwnerIdRejected(params.getOwnerId(), params.getPageable());
    }

    @Override
    public BookingStateDto getType() {
        return BookingStateDto.REJECTED;
    }
}

package ru.practicum.shareit.booking.search.owner.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
public class BookingSearchByOwnerParams {

    private final Long ownerId;

    private final Pageable pageable;

}

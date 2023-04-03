package ru.practicum.shareit.booking.search.booker.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.model.User;

@Getter
@AllArgsConstructor
public class BookingSearchByBookerParams {

    private final User booker;

    private final Pageable pageable;

}

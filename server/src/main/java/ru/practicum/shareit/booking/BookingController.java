package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.CustomPageRequest;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";


    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId) {
        log.info("GET /bookings/{} | userId: {}", bookingId, userId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping()
    public List<BookingDto> getAllByBookerId(@RequestHeader(USER_ID_HEADER) Long bookerId,
            @RequestParam(name = "state", defaultValue = "all") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /bookings?state={}&from={}&size={} | bookerId: {}", state, from, size, bookerId);
        return bookingService.getAllByBookerId(bookerId, state,
                new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "start")));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(name = "state", defaultValue = "all") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /bookings/owner?state={}&from={}&size={} | ownerId: {}", state, from, size, ownerId);
        return bookingService.getAllByOwnerId(ownerId, state,
                new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "start")));
    }

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                             @RequestBody BookingDto bookingDto) {
        log.info("POST /bookings | bookerId: {} | bookingDto: {}", bookerId, bookingDto);
        bookingDto.setBookerId(bookerId);
        return bookingService.create(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}?approved={} | ownerId: {}", bookingId, approved, ownerId);
        return bookingService.approve(bookingId, ownerId, approved);
    }
}

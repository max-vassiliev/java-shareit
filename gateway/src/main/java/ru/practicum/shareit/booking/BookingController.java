package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;

	private static final String USER_ID_HEADER = "X-Sharer-User-Id";


	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
										  @PathVariable Long bookingId) {
		log.info("GET /bookings/{} | userId: {}", bookingId, userId);
		return bookingClient.getById(bookingId, userId);
	}

	@GetMapping()
	public ResponseEntity<Object> getAllByBookerId(@RequestHeader(USER_ID_HEADER) Long bookerId,
								 	@RequestParam(name = "state", defaultValue = "all") String stateParam,
									@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
									@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		log.info("GET /bookings?state={}&from={}&size={} | bookerId: {}", stateParam, from, size, bookerId);
		BookingState state = BookingState.fromString(stateParam);
		return bookingClient.getAllByBookerId(bookerId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId,
											@RequestParam(name = "state", defaultValue = "all") String stateParam,
											@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
											@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		log.info("GET /bookings/owner?state={}&from={}&size={} | ownerId: {}", stateParam, from, size, ownerId);
		BookingState state = BookingState.fromString(stateParam);
		return bookingClient.getAllByOwnerId(ownerId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long bookerId,
										 @Validated @RequestBody BookItemRequestDto bookingDto) {
		log.info("POST /bookings | bookerId: {} | bookingDto: {}", bookerId, bookingDto);
		return bookingClient.create(bookerId, bookingDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader(USER_ID_HEADER) Long ownerId,
							  @PathVariable Long bookingId,
							  @RequestParam Boolean approved) {
		log.info("PATCH /bookings/{}?approved={} | ownerId: {}", bookingId, approved, ownerId);
		return bookingClient.approve(bookingId, ownerId, approved);
	}
}

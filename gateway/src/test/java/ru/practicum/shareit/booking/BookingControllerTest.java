package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;


    @Test
    @SneakyThrows
    void create_whenItemUnavailable_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookingDto inputDto = createBookingDto();

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenItemNotFound_thenStatusIsNotFound() {
        Long userId = 1L;
        BookingDto inputDto = createBookingDto();

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenUserNotFound_thenStatusIsNotFound() {
        Long userId = 1000L;
        BookingDto inputDto = createBookingDto();

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenStartIsNull_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookItemRequestDto inputDto = createBookItemRequestDto(
                null,
                LocalDateTime.now().plusDays(3)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenEndIsNull_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookItemRequestDto inputDto = createBookItemRequestDto(
                LocalDateTime.now().plusDays(1),
                null
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenEndInPast_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookItemRequestDto inputDto = createBookItemRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenEndBeforeStart_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookingDto inputDto = createBookingDto(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1)
        );

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenStartInPast_thenStatusIsBadRequest() {
        Long userId = 1L;
        BookingDto inputDto = createBookingDto(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenValid_thenStatusIsOkAndDtoReturned() {
        Long expectedBookingId = 1L;
        BookingDto inputDto = createBookingDto();
        BookingDto outputDto = createBookingDtoOut();

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, outputDto.getBooker().getId())
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedBookingId), Long.class))
                .andExpect(jsonPath("$.start", is(outputDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())));

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void create_whenBookerIsOwner_thenStatusIsNotFound() {
        Long bookerId = 1L;
        BookingDto inputDto = createBookingDto();
        inputDto.setBookerId(bookerId);

        when(bookingClient.create(isA(long.class), isA(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .create(isA(long.class), isA(BookItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void approve_whenValidAndApproved_thenStatusIsOkAndDtoReturnedWithStatusApproved() {
        BookingDto outputDto = createBookingDtoOut();
        outputDto.setStatus(BookingState.APPROVED);
        Long ownerId = 1L;
        Long bookingId = outputDto.getId();
        String approved = "true";

        when(bookingClient.approve(isA(Long.class), isA(Long.class), isA(boolean.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, ownerId)
                        .param("approved", approved))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(outputDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())));

        verify(bookingClient, times(1))
                .approve(isA(Long.class), isA(Long.class), isA(boolean.class));
    }

    @Test
    @SneakyThrows
    void approve_whenValidAndRejected_thenStatusIsOkAndDtoReturnedWithStatusRejected() {
        BookingDto outputDto = createBookingDtoOut();
        outputDto.setStatus(BookingState.REJECTED);
        Long ownerId = 1L;
        Long bookingId = outputDto.getId();
        String approved = "false";

        when(bookingClient.approve(isA(Long.class), isA(Long.class), isA(boolean.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, ownerId)
                        .param("approved", approved))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(outputDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())));

        verify(bookingClient, times(1))
                .approve(isA(Long.class), isA(Long.class), isA(boolean.class));
    }

    @Test
    @SneakyThrows
    void approve_whenUserNotOwner_thenStatusIsBadRequest() {
        Long userId = 2L;
        Long bookingId = 1L;
        String approved = "true";

        when(bookingClient.approve(isA(Long.class), isA(Long.class), isA(boolean.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest());

        verify(bookingClient, times(1))
                .approve(isA(Long.class), isA(Long.class), isA(boolean.class));
    }

    @Test
    @SneakyThrows
    void approve_whenUserIsBooker_thenStatusIsNotFound() {
        Long userId = 2L;
        Long bookingId = 1L;
        String approved = "true";

        when(bookingClient.approve(isA(Long.class), isA(Long.class), isA(boolean.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", approved))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .approve(isA(Long.class), isA(Long.class), isA(boolean.class));
    }

    @Test
    @SneakyThrows
    void approve_whenBookingApprovedBefore_thenStatusIsBadRequest() {
        Long userId = 1L;
        Long bookingId = 1L;
        String approved = "true";

        when(bookingClient.approve(isA(Long.class), isA(Long.class), isA(boolean.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest());

        verify(bookingClient, times(1))
                .approve(isA(Long.class), isA(Long.class), isA(boolean.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsBooker_thenStatusIsOkAndDtoReturned() {
        BookingDto outputDto = createBookingDtoOut();
        Long bookingId = outputDto.getId();
        Long bookerId = outputDto.getBooker().getId();

        when(bookingClient.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.start", is(outputDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookerId), Long.class))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())));

        verify(bookingClient, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsOwner_thenStatusIsOkAndDtoReturned() {
        BookingDto outputDto = createBookingDtoOut();
        Long bookingId = outputDto.getId();
        Long ownerId = 1L;

        when(bookingClient.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.start", is(outputDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())));

        assertNotEquals(ownerId, outputDto.getBooker().getId());
        verify(bookingClient, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenBookingNotFound_thenStatusIsNotFound() {
        Long bookingId = 1000L;
        Long userId = 2L;

        when(bookingClient.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsNotBookerOrOwner_thenStatusIsNotFound() {
        Long bookingId = 1L;
        Long userId = 3L;

        when(bookingClient.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenUserNotFound_thenStatusIsNotFound() {
        Long bookerId = 1000L;

        when(bookingClient.getAllByBookerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenStateInvalid_thenStatusIsBadRequest() {
        Long bookerId = 1L;
        String invalidState = "unknown";

        when(bookingClient.getAllByBookerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", invalidState))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenSizeIsZero_thenStatusIsBadRequest() {
        Long bookerId = 2L;
        String invalidSize = "0";

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("size", invalidSize))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenSizeIsNegative_thenStatusIsBadRequest() {
        Long bookerId = 2L;
        String invalidSize = "-1";

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("size", invalidSize))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenFromIsNegative_thenStatusIsBadRequest() {
        Long bookerId = 2L;
        String invalidFrom = "-1";

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("from", invalidFrom))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenUserNotFound_thenStatusIsNotFound() {
        Long ownerId = 1000L;

        when(bookingClient.getAllByOwnerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isNotFound());

        verify(bookingClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenStateInvalid_thenStatusIsBadRequest() {
        Long ownerId = 1L;
        String invalidState = "unknown";

        when(bookingClient.getAllByOwnerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("state", invalidState))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenSizeIsZero_thenStatusIsBadRequest() {
        Long ownerId = 1L;
        String invalidSize = "0";

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("size", invalidSize))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenSizeIsNegative_thenStatusIsBadRequest() {
        Long ownerId = 1L;
        String invalidSize = "-1";

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("size", invalidSize))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenFromIsNegative_thenStatusIsBadRequest() {
        Long ownerId = 1L;
        String invalidFrom = "-1";

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("from", invalidFrom))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenValid_thenStatusIsOkAndDtosReturned() {
        Long bookerId = 2L;
        int expectedCount = 3;
        List<BookingDto> bookings = createBookingDtosByBooker();

        when(bookingClient.getAllByBookerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].status", is(bookings.get(1).getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())))
                .andExpect(jsonPath("$[2].id", is(bookings.get(2).getId()), Long.class))
                .andExpect(jsonPath("$[2].start", is(bookings.get(2).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[2].end", is(bookings.get(2).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[2].status", is(bookings.get(2).getStatus().toString())))
                .andExpect(jsonPath("$[2].booker.id", is(bookings.get(2).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[2].item.id", is(bookings.get(2).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[2].item.name", is(bookings.get(2).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenValidAndStateIsAll_thenStatusIsOkAndAllDtosReturned() {
        Long bookerId = 2L;
        String state = "all";
        int expectedCount = 3;
        List<BookingDto> bookings = createBookingDtosByBooker();

        when(bookingClient.getAllByBookerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].status", is(bookings.get(1).getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())))
                .andExpect(jsonPath("$[2].id", is(bookings.get(2).getId()), Long.class))
                .andExpect(jsonPath("$[2].start", is(bookings.get(2).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[2].end", is(bookings.get(2).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[2].status", is(bookings.get(2).getStatus().toString())))
                .andExpect(jsonPath("$[2].booker.id", is(bookings.get(2).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[2].item.id", is(bookings.get(2).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[2].item.name", is(bookings.get(2).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByBookerId_whenAllIs3FromIs1AndSizeIs2_thenStatusIsOkAnd2DtosReturned() {
        Long bookerId = 2L;
        String from = "1";
        String size = "2";
        int expectedCount = 2;
        List<BookingDto> bookings = createBookingDtosByBooker();
        bookings.remove(0);

        when(bookingClient.getAllByBookerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].status", is(bookings.get(1).getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByBookerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenValid_thenStatusIsOkAndDtosReturned() {
        Long ownerId = 2L;
        int expectedCount = 2;
        List<BookingDto> bookings = createBookingDtosByOwner();

        when(bookingClient.getAllByOwnerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].status", is(bookings.get(1).getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenValidAndStateIsAll_thenStatusIsOkAndAllDtosReturned() {
        Long ownerId = 2L;
        String state = "all";
        int expectedCount = 2;
        List<BookingDto> bookings = createBookingDtosByOwner();

        when(bookingClient.getAllByOwnerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].status", is(bookings.get(1).getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenAllIs2FromIs1AndSizeIs1_thenStatusIsOkAndListWith1DtoReturned() {
        Long ownerId = 2L;
        String from = "1";
        String size = "1";
        int expectedCount = 1;
        List<BookingDto> bookings = createBookingDtosByOwner();
        bookings.remove(0);

        when(bookingClient.getAllByOwnerId(isA(Long.class), isA(BookingState.class),
                isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(bookings, HttpStatus.OK));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("from", from)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookings.get(0).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookings.get(0).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].status", is(bookings.get(0).getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())));

        verify(bookingClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(BookingState.class), isA(Integer.class), isA(Integer.class));
    }


    // ----------
    // Шаблоны
    // ----------

    private BookingDto createBookingDto(LocalDateTime start, LocalDateTime end) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    private BookItemRequestDto createBookItemRequestDto(LocalDateTime start, LocalDateTime end) {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    private BookingDto createBookingDto() {
        return createBookingDto(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5)
        );
    }

    private BookingDto createBookingDtoOut() {
        ItemDto itemDto = createItemDto();
        UserDto bookerDto = createUserDto(2L, "Kate", "kate@example.com");
        BookingDto outputDto = createBookingDto();
        outputDto.setId(1L);
        outputDto.setStatus(BookingState.WAITING);
        outputDto.setItem(itemDto);
        outputDto.setBooker(bookerDto);
        outputDto.setBookerId(bookerDto.getId());
        outputDto.setItemId(itemDto.getId());
        outputDto.setItemName(itemDto.getName());
        return outputDto;
    }

    private BookingDto createBookingDtoOut(Long id, ItemDto itemDto, UserDto bookerDto,
                                           LocalDateTime start, LocalDateTime end) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(id);
        bookingDto.setStatus(BookingState.WAITING);
        bookingDto.setItem(itemDto);
        bookingDto.setBooker(bookerDto);
        bookingDto.setBookerId(bookerDto.getId());
        bookingDto.setItemId(itemDto.getId());
        bookingDto.setItemName(itemDto.getName());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    private List<BookingDto> createBookingDtosByOwner() {
        ItemDto item = createItemDto();
        UserDto booker1 = createUserDto(2L, "Kate", "kate@example.com");
        UserDto booker2 = createUserDto(3L, "Paul", "paul@example.com");

        BookingDto bookingDto1 = createBookingDtoOut(1L, item, booker1,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        BookingDto bookingDto2 = createBookingDtoOut(2L, item, booker2,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7));

        return new ArrayList<>(Arrays.asList(bookingDto1, bookingDto2));
    }

    private List<BookingDto> createBookingDtosByBooker() {
        UserDto booker = createUserDto(2L, "Kate", "kate@example.com");
        ItemDto item1 = createItemDto();
        ItemDto item2 = createItemDto2();

        BookingDto bookingDto1 = createBookingDtoOut(1L, item1, booker,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        bookingDto1.setStatus(BookingState.APPROVED);

        BookingDto bookingDto2 = createBookingDtoOut(2L, item2, booker,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(5));

        BookingDto bookingDto3 = createBookingDtoOut(3L, item1, booker,
                LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8));
        bookingDto1.setStatus(BookingState.APPROVED);

        return new ArrayList<>(Arrays.asList(bookingDto1, bookingDto2, bookingDto3));
    }

    private UserDto createUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private ItemDto createItemDto2() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(2L);
        itemDto.setName("Paul's Item 1");
        itemDto.setDescription("Paul's Item 1 Description");
        itemDto.setAvailable(true);
        return itemDto;
    }
}
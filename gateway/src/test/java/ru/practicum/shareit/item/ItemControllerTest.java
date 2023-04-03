package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;


    @Test
    @SneakyThrows
    void add_whenValid_thenStatusIsOkAndDtoReturned() {
        Long expectedItemId = 1L;
        Long ownerId = 1L;
        ItemDto inputDto = createItemDto();
        ItemDto outputDto = createItemDto();
        outputDto.setId(expectedItemId);

        when(itemClient.save(isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedItemId), Long.class))
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.description", is(inputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(inputDto.getAvailable())))
                .andExpect(jsonPath("$.requestId").isEmpty())
                .andExpect(jsonPath("$.ownerId").doesNotExist());

        verify(itemClient, times(1)).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenValidAndContainsRequestId_thenStatusIsOkAndDtoWithRequestIdReturned() {
        Long expectedItemId = 1L;
        Long requestId = 1L;
        Long ownerId = 1L;

        ItemDto inputDto = createItemDto();
        inputDto.setRequestId(requestId);
        ItemDto outputDto = createItemDto();
        outputDto.setId(expectedItemId);
        outputDto.setRequestId(requestId);

        when(itemClient.save(isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedItemId), Long.class))
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.description", is(inputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(inputDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(requestId), Long.class))
                .andExpect(jsonPath("$.ownerId").doesNotExist());

        verify(itemClient, times(1)).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenUsedIdNotPassed_thenReturnInternalServerError() {
        ItemDto inputDto = createItemDto();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenUsedIdNotFound_thenReturnNotFound() {
        Long ownerId = 100L;
        ItemDto inputDto = createItemDto();

        when(itemClient.save(isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemClient, times(1)).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenRespondingToOwnRequest_thenReturnBadRequest() {
        Long ownerId = 1L;
        Long requestId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setRequestId(requestId);

        when(itemClient.save(isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, times(1)).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenAvailableIsNull_thenReturnBadRequest() {
        Long ownerId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setAvailable(null);

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenNameIsEmpty_thenReturnBadRequest() {
        Long ownerId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setName("");

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void add_whenDescriptionIsEmpty_thenReturnBadRequest() {
        Long ownerId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setDescription("");

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).save(isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenValid_thenStatusIsOkAndDtoReturned() {
        Long itemId = 1L;
        Long ownerId = 1L;
        ItemDto inputDto = createItemDto();
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.update(isA(Long.class), isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.description", is(inputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(inputDto.getAvailable())))
                .andExpect(jsonPath("$.requestId").isEmpty())
                .andExpect(jsonPath("$.ownerId").doesNotExist());

        verify(itemClient, times(1))
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenUserIdNotPassed_thenReturnInternalServerError() {
        Long itemId = 1L;
        ItemDto inputDto = createItemDto();

        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never())
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenInvokedByOtherUser_thenReturnForbidden() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto inputDto = createItemDto();

        when(itemClient.update(isA(Long.class), isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(itemClient, times(1))
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenAvailableUpdated_thenStatusIsOkAndDtoReturned() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setAvailable(true);
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.update(isA(Long.class), isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.available", is(inputDto.getAvailable())))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())));

        verify(itemClient, times(1))
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenNameUpdated_thenStatusIsOkAndDtoReturned() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Peter's Item 1");
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.update(isA(Long.class), isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())));

        verify(itemClient, times(1))
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void update_whenDescriptionUpdated_thenStatusIsOkAndDtoReturned() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setDescription("Peter's Item 1 Description");
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.update(isA(Long.class), isA(Long.class), isA(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.description", is(inputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())))
                .andExpect(jsonPath("$.name", is(outputDto.getName())));

        verify(itemClient, times(1))
                .update(isA(Long.class), isA(Long.class), isA(ItemDto.class));
    }

    @Test
    @SneakyThrows
    void getById_whenValid_thenStatusIsOkAndDtoReturned() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())));

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsNotOwner_thenStatusIsOkAndDtoReturned() {
        Long userId = 2L;
        Long itemId = 1L;
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())));

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenInvokedByOwner_thenStatusIsOkAndDtoWithBookingsReturned() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto outputDto = createItemDtoWithBookings();

        when(itemClient.getByIdAndUserId(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.lastBooking.id",
                        is(outputDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId",
                        is(outputDto.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.nextBooking.id",
                        is(outputDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(outputDto.getNextBooking().getBookerId()), Long.class));

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsNotOwner_thenStatusIsOkAndDtoWithoutBookingsReturned() {
        Long userId = 2L;
        Long itemId = 1L;
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.getByIdAndUserId(isA(Long.class), isA(Long.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty());

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenItemNotFound_thenStatusIsNotFound() {
        Long userId = 1L;
        Long itemId = 1L;

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenItemHasNoComments_thenStatusIsOkAndCommentsListIsEmpty() {
        Long itemId = 1L;
        Long ownerId = 1L;
        ItemDto outputDto = createItemDto();
        outputDto.setId(itemId);

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(outputDto.getAvailable())))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments", hasSize(0)));

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserNotOwnerAndItemHasComments_thenStatusIsOkAndDtoWithCommentsReturned() {
        Long itemId = 1L;
        Long userId = 2L;
        ItemDto itemDto = createItemDto();
        CommentDto commentDto = createCommentDtoOut(1L, "Item 2 Comment");
        itemDto.setId(itemId);
        itemDto.getComments().add(commentDto);

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id",
                        is(itemDto.getComments().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text",
                        is(itemDto.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName",
                        is(itemDto.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created").exists());

        verify(itemClient, times(1))
                .getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIsOwnerAndItemHasComments_thenStatusIsOkAndDtoWithCommentsAndBookingsReturned() {
        Long itemId = 1L;
        Long ownerId = 1L;
        ItemDto itemDto = createItemDto();
        CommentDto commentDto = createCommentDtoOut(1L, "Item 2 Comment");
        BookingDto lastBooking = createBookingDto(1L, itemId, 2L);
        BookingDto nextBooking = createBookingDto(2L, itemId, 3L);
        itemDto.setId(itemId);
        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);
        itemDto.getComments().add(commentDto);

        when(itemClient.getByIdAndUserId(any(), any()))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.lastBooking.id",
                        is(itemDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId",
                        is(itemDto.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.nextBooking.id",
                        is(itemDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(itemDto.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id",
                        is(itemDto.getComments().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text",
                        is(itemDto.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName",
                        is(itemDto.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created").exists());

        verify(itemClient, times(1)).getByIdAndUserId(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenValid_thenStatusIsOkAndDtosReturned() {
        Long ownerId = 1L;
        List<ItemDto> itemDtos = createItemDtos();

        when(itemClient.getAllByOwnerId(isA(Long.class), isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(itemDtos, HttpStatus.OK));

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(itemDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtos.get(0).getAvailable())))
                .andExpect(jsonPath("$[1].id", is(itemDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(itemDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDtos.get(1).getAvailable())))
                .andExpect(jsonPath("$[2].id", is(itemDtos.get(2).getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(itemDtos.get(2).getName())))
                .andExpect(jsonPath("$[2].description", is(itemDtos.get(2).getDescription())))
                .andExpect(jsonPath("$[2].available", is(itemDtos.get(2).getAvailable())));

        verify(itemClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByOwnerId_whenValid_thenStatusIsOkAndDtosWithBookingReturned() {
        Long ownerId = 1L;
        List<ItemDto> itemDtos = createItemDtosWithBookings();

        when(itemClient.getAllByOwnerId(isA(Long.class), isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(itemDtos, HttpStatus.OK));

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtos.get(0).getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking").exists())
                .andExpect(jsonPath("$[0].lastBooking.id", is(itemDtos.get(0).getLastBooking().getId()),
                        Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(itemDtos.get(0).getLastBooking()
                        .getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking").exists())
                .andExpect(jsonPath("$[0].nextBooking.id", is(itemDtos.get(0).getNextBooking().getId()),
                        Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(itemDtos.get(0).getNextBooking()
                        .getBookerId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(itemDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDtos.get(1).getAvailable())))
                .andExpect(jsonPath("$[1].lastBooking").isEmpty())
                .andExpect(jsonPath("$[1].nextBooking").isEmpty());

        verify(itemClient, times(1))
                .getAllByOwnerId(isA(Long.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenValid_thenStatusIsOkAndDtosReturned() {
        Long userId = 1L;
        List<ItemDto> itemDtos = createItemDtos();
        List<ItemDto> outputDtos = new ArrayList<>(Arrays.asList(itemDtos.get(1), itemDtos.get(2)));

        when(itemClient.getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(outputDtos, HttpStatus.OK));

        mvc.perform(get("/items/search?text={text}", "keYwOrd")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(outputDtos.size())))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(outputDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].available", is(outputDtos.get(0).getAvailable())))
                .andExpect(jsonPath("$[1].id", is(outputDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(outputDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(outputDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].available", is(outputDtos.get(1).getAvailable())));

        verify(itemClient, times(1))
                .getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenValidButItemsNotFound_thenStatusIsOkAndEmptyListReturned() {
        Long userId = 1L;

        when(itemClient.getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));

        mvc.perform(get("/items/search?text={text}", "quErY")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemClient, times(1))
                .getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void getAllByKeyword_whenKeywordIsEmpty_thenStatusIsOkAndEmptyListReturned() {
        Long userId = 1L;

        when(itemClient.getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class)))
                .thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));

        mvc.perform(get("/items/search?text={text}", "")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemClient, times(1))
                .getAllByKeyword(isA(String.class), isA(Integer.class), isA(Integer.class));
    }

    @Test
    @SneakyThrows
    void addComment_whenNoPriorBooking_thenStatusIsBadRequest() {
        Long itemId = 1L;
        Long userId = 2L;
        CommentDto commentDto = createCommentDto("Comment to Item 1");

        when(itemClient.saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, times(1))
                .saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class));
    }

    @Test
    @SneakyThrows
    void addComment_whenTextIsEmpty_thenStatusIsBadRequest() {
        Long itemId = 1L;
        Long userId = 2L;
        CommentDto commentDto = createCommentDto("");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never())
                .saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class));
    }

    @Test
    @SneakyThrows
    void addComment_whenValid_thenStatusIsOkAndDtoReturned() {
        Long itemId = 1L;
        Long userId = 2L;
        Long expectedCommentId = 1L;
        CommentDto inputDto = createCommentDto("Comment to Item 1");
        CommentDto outputDto = createCommentDtoOut(expectedCommentId, inputDto.getText());

        when(itemClient.saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(outputDto, HttpStatus.OK));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedCommentId), Long.class))
                .andExpect(jsonPath("$.text", is(inputDto.getText())))
                .andExpect(jsonPath("$.authorName", is(outputDto.getAuthorName())))
                .andExpect(jsonPath("$.created").exists());

        verify(itemClient, times(1))
                .saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class));
    }

    @Test
    @SneakyThrows
    void addComment_whenBookingInFuture_thenStatusIsBadRequest() {
        Long itemId = 1L;
        Long userId = 2L;
        CommentDto commentDto = createCommentDto("Comment to Item 1");

        when(itemClient.saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, times(1))
                .saveComment(isA(Long.class), isA(Long.class), isA(CommentDto.class));
    }

    // ----------
    // Шаблоны
    // ----------

    private ItemDto createItemDto(Long id, String name, String description) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);
        return itemDto;
    }

    private List<ItemDto> createItemDtos() {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Peter's Item 1");
        item1.setDescription("Peter's Item 1 Description");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Peter's Item 2 (keyword)");
        item2.setDescription("Peter's Item 2 Description");
        item2.setAvailable(true);

        ItemDto item3 = new ItemDto();
        item3.setId(3L);
        item3.setName("Peter's Item 3");
        item3.setDescription("Peter's Item 3 Description (keyword)");
        item3.setAvailable(true);

        return new ArrayList<>(Arrays.asList(item1, item2, item3));
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private ItemDto createItemDtoWithBookings() {
        Long itemId = 1L;
        ItemDto itemDto = createItemDto();
        itemDto.setId(itemId);

        BookingDto lastBooking = createBookingDto(1L, itemId, 2L);
        BookingDto nextBooking = createBookingDto(2L, itemId, 3L);
        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);

        return itemDto;
    }

    private List<ItemDto> createItemDtosWithBookings() {
        ItemDto item1 = createItemDto(1L,
                "Peter's Item 1",
                "Peter's Item 1 Description"
        );

        BookingDto lastBookingItem1 = createBookingDto(1L, 1L, 2L);
        BookingDto nextBookingItem1 = createBookingDto(2L, 1L, 3L);
        item1.setLastBooking(lastBookingItem1);
        item1.setNextBooking(nextBookingItem1);

        ItemDto item2 = createItemDto(2L,
                "Peter's Item 2",
                "Peter's Item 2 Description"
        );

        return new ArrayList<>(Arrays.asList(item1, item2));
    }

    private BookingDto createBookingDto(Long id, Long itemId, Long bookerId) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(id);
        bookingDto.setItemId(itemId);
        bookingDto.setBookerId(bookerId);
        return bookingDto;
    }

    private CommentDto createCommentDto(String text) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        return commentDto;
    }

    private CommentDto createCommentDtoOut(Long id, String text) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(id);
        commentDto.setText(text);
        commentDto.setAuthorName("Kate");
        commentDto.setCreated(LocalDateTime.now());
        return commentDto;
    }
}
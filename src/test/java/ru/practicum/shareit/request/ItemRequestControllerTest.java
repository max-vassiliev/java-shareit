package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService requestService;


    @Test
    @SneakyThrows
    void save_whenValid_thenStatusIsOkAndDtoReturned() {
        Long requestorId = 2L;
        ItemRequestDto inputDto = createInputDto();
        ItemRequestDto outputDto = createItemRequestDto(1L,
                inputDto.getDescription(),
                requestorId,
                LocalDateTime.now());

        when(requestService.save(isA(ItemRequestDto.class)))
                .thenReturn(outputDto);

        mvc.perform(post("/requests")
                    .header(USER_ID_HEADER, requestorId)
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(outputDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(outputDto.getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1)).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenDescriptionIsEmpty_thenReturnBadRequest() {
        ItemRequestDto inputDto = new ItemRequestDto();
        inputDto.setDescription("");

        mvc.perform(post("/requests")
                    .header(USER_ID_HEADER, 1L)
                    .content(mapper.writeValueAsString(inputDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenDescriptionIsNull_thenReturnBadRequest() {
        ItemRequestDto inputDto = new ItemRequestDto();

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenNoRequestBody_thenReturnInternalServerError() {
        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isInternalServerError());

        verify(requestService, never()).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenNoHeader_thenStatusIsInternalServerError() {
        ItemRequestDto inputDto = createInputDto();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(requestService, never()).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenUserIdIsZero_thenStatusIsInternalServerError() {
        Long requestorId = 0L;
        ItemRequestDto inputDto = createInputDto();

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, requestorId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(requestService, never()).save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenUserIdIsNegative_thenStatusIsInternalServerError() {
        Long requestorId = -1L;
        ItemRequestDto inputDto = createInputDto();

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, requestorId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void save_whenUserNotFound_thenStatusIsNotFound() {
        Long requestorId = 2L;
        ItemRequestDto inputDto = createInputDto();

        when(requestService.save(isA(ItemRequestDto.class)))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, requestorId)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(requestService, times(1))
                .save(isA(ItemRequestDto.class));
    }

    @Test
    @SneakyThrows
    void getById_whenValid_thenStatusIsOkAndDtoReturned() {
        ItemRequestDto outputDto = createOutputDto();
        Long requestId = outputDto.getId();
        Long userId = outputDto.getRequestorId();

        when(requestService.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(outputDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(outputDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(outputDto.getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenInvokedByOtherUser_thenStatusIsOkAndDtoReturned() {
        ItemRequestDto outputDto = createOutputDto();
        Long requestId = outputDto.getId();
        Long userId = 3L;

        when(requestService.getById(isA(Long.class), isA(Long.class)))
                .thenReturn(outputDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(outputDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(outputDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(outputDto.getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getById(isA(Long.class), isA(Long.class));
        assertNotEquals(userId, outputDto.getRequestorId());
    }

    @Test
    @SneakyThrows
    void getById_whenUserIdIsZero_thenStatusIsInternalServerError() {
        Long userId = 0L;
        Long requestId = 1L;

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserIdIsNegative_thenStatusIsInternalServerError() {
        Long userId = -1L;
        Long requestId = 1L;

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenUserNotFound_thenStatusIsNotFound() {
        Long userId = 1000L;
        Long requestId = 1L;

        when(requestService.getById(isA(Long.class), isA(Long.class)))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(requestService, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getById_whenRequestNotFound_thenStatusIsNotFound() {
        Long userId = 1L;
        Long requestId = 1000L;

        when(requestService.getById(isA(Long.class), isA(Long.class)))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(requestService, times(1))
                .getById(isA(Long.class), isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByRequestorId_whenValid_thenStatusIsOkAndDtosReturned() {
        int expectedCount = 2;
        Long requestorId = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestor(requestorId);

        when(requestService.getAllByRequestorId(isA(Long.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].id", is(outputDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(outputDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].requestorId", is(outputDtos.get(1).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[1].created", is(outputDtos.get(1).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getAllByRequestorId(isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByRequestorId_whenNoRequests_thenStatusIsOkAndEmptyListReturned() {
        int expectedCount = 0;
        Long requestorId = 2L;

        when(requestService.getAllByRequestorId(isA(Long.class)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)));

        verify(requestService, times(1))
                .getAllByRequestorId(isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByRequestorId_whenRequestsWithItems_thenStatusIsOkAndDtosWithItemsReturned() {
        int expectedRequests = 2;
        int expectedItems = 1;

        Long requestorId = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestorWithItems(requestorId);

        when(requestService.getAllByRequestorId(isA(Long.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedRequests)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].items[0].id", is(outputDtos.get(0)
                        .getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(outputDtos.get(0)
                        .getItems().get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(outputDtos.get(0)
                        .getItems().get(0).getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(outputDtos.get(0)
                        .getItems().get(0).getAvailable())))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(outputDtos.get(0)
                        .getItems().get(0).getOwnerId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(outputDtos.get(0)
                        .getItems().get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(outputDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(outputDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].requestorId", is(outputDtos.get(1).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[1].created", is(outputDtos.get(1).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getAllByRequestorId(isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByRequestorId_whenUserNotFound_thenStatusIsNotFound() {
        Long requestorId = 1000L;

        when(requestService.getAllByRequestorId(isA(Long.class)))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, requestorId))
                .andExpect(status().isNotFound());

        verify(requestService, times(1))
                .getAllByRequestorId(isA(Long.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenValid_thenStatusIsOkAndDtosReturned() {
        int expectedCount = 2;
        Long userId = 1L;
        Long otherUser = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestor(otherUser);

        when(requestService.getAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(userId), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].id", is(outputDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(outputDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].requestorId", is(outputDtos.get(1).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[1].requestorId", not(userId), Long.class))
                .andExpect(jsonPath("$[1].created", is(outputDtos.get(1).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIs2AndSizeIs1_thenStatusIsOkAndListWith1DtoReturned() {
        int expectedCount = 1;
        String size = "1";
        Long userId = 1L;
        Long otherUser = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestor(otherUser);
        outputDtos.remove(1);

        when(requestService.getAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(userId), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIs2AndFromIs1_thenStatusIsOkAndListWith1DtoReturned() {
        int expectedCount = 1;
        String from = "1";
        Long userId = 1L;
        Long otherUser = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestor(otherUser);
        outputDtos.remove(1);

        when(requestService.getAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", from))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(userId), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(requestService, times(1))
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIs2SizeIs1AndItemsPresent_thenStatusIsOkAndListWith1DtoWithItemsReturned() {
        int expectedRequests = 1;
        int expectedItems = 1;
        String size = "1";
        Long userId = 1L;
        Long otherUser = 2L;
        List<ItemRequestDto> outputDtos = createDtosForRequestorWithItems(otherUser);
        outputDtos.remove(1);

        when(requestService.getAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(outputDtos);

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedRequests)))
                .andExpect(jsonPath("$[0].id", is(outputDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(outputDtos.get(0).getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].requestorId", not(userId), Long.class))
                .andExpect(jsonPath("$[0].created", is(outputDtos.get(0).getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(expectedItems)))
                .andExpect(jsonPath("$[0].items[0].id", is(outputDtos.get(0)
                        .getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(outputDtos.get(0)
                        .getItems().get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(outputDtos.get(0)
                        .getItems().get(0).getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(outputDtos.get(0)
                        .getItems().get(0).getAvailable())))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(outputDtos.get(0)
                        .getItems().get(0).getOwnerId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(outputDtos.get(0)
                        .getItems().get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(outputDtos.get(0).getId()), Long.class));

        verify(requestService, times(1))
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenAllIsEmptyAndSizeIs1_thenStatusIsOkAndEmptyListReturned() {
        int expectedCount = 0;
        String size = "1";
        Long userId = 1L;

        when(requestService.getAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedCount)));

        verify(requestService, times(1))
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenSizeIsZero_thenStatusIsInternalServerError() {
        String size = "0";
        Long userId = 1L;

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("size", size))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenSizeIsNegative_thenStatusIsInternalServerError() {
        String size = "-1";
        Long userId = 1L;

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("size", size))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }

    @Test
    @SneakyThrows
    void getAllByOtherUsers_whenFromIsNegative_thenStatusIsInternalServerError() {
        String from = "-1";
        Long userId = 1L;

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId)
                        .param("from", from))
                .andExpect(status().isInternalServerError());

        verify(requestService, never())
                .getAllByOtherUsers(isA(Long.class), isA(Pageable.class));
    }


    // ----------
    // Шаблоны
    // ----------

    private List<ItemRequestDto> createDtosForRequestor(Long requestorId) {
        ItemRequestDto request1 = createItemRequestDto(
                1L,
                "Description Item Request 1",
                requestorId,
                LocalDateTime.now());

        ItemRequestDto request2 = createItemRequestDto(
                2L,
                "Description Item Request 2",
                requestorId,
                LocalDateTime.now());

        return new ArrayList<>(Arrays.asList(request1, request2));
    }

    private List<ItemRequestDto> createDtosForRequestorWithItems(Long requestorId) {
        List<ItemRequestDto> requestDtos = createDtosForRequestor(requestorId);

        ItemItemRequestDto item1 = createItemDto(
                requestDtos.get(0).getId());
        requestDtos.get(0).getItems().add(item1);

        return requestDtos;
    }

    private ItemItemRequestDto createItemDto(Long requestId) {
        ItemItemRequestDto itemDto = new ItemItemRequestDto();
        itemDto.setId(1L);
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    private ItemRequestDto createItemRequestDto(Long id, String description, Long requestorId, LocalDateTime created) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setRequestorId(requestorId);
        dto.setCreated(created);
        return dto;
    }

    private ItemRequestDto createOutputDto() {
        return createItemRequestDto(1L,
                "Description Item Request 1",
                2L,
                LocalDateTime.now());
    }

    private ItemRequestDto createInputDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Description Item Request 1");
        return dto;
    }
}
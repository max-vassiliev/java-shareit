package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Spy
    private ItemRequestMapper requestMapper = Mappers.getMapper(ItemRequestMapper.class);

    @Spy
    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private static final Pageable DEFAULT_PAGEABLE = new CustomPageRequest(0, 10,
            Sort.by(Sort.Direction.DESC, "created"));


    @Test
    void save_whenValid_thenDtoWithIdReturned() {
        Long expectedId = 1L;
        User requestor = createRequestor();
        ItemRequestDto inputDto = createInputDto(requestor.getId());
        ItemRequest savedRequest = createItemRequest(expectedId, inputDto, requestor);

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(requestor));
        when(requestRepository.save(isA(ItemRequest.class)))
                .thenReturn(savedRequest);

        ItemRequestDto outputDto = requestService.save(inputDto);

        assertEquals(expectedId, outputDto.getId());
        assertEquals(inputDto.getDescription(), outputDto.getDescription());
        assertEquals(inputDto.getRequestorId(), outputDto.getRequestorId());
        assertNotNull(outputDto.getCreated());
        assertInstanceOf(LocalDateTime.class, outputDto.getCreated());
        assertTrue(outputDto.getCreated().isBefore(LocalDateTime.now().plusMinutes(1)));
        assertTrue(outputDto.getCreated().isAfter(LocalDateTime.now().minusMinutes(2)));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .save(isA(ItemRequest.class));
    }

    @Test
    void save_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        Long requestorId = 1000L;
        ItemRequestDto inputDto = createInputDto(requestorId);
        String expectedExceptionMessage = "Не найден пользователь с ID " + requestorId;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> requestService.save(inputDto));
        assertEquals(expectedExceptionMessage, exception.getMessage());
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, never())
                .save(isA(ItemRequest.class));
    }

    @Test
    void getById_whenValidAndRequestWithItems_thenDtoWithItemsReturned() {
        int expectedItems = 1;
        Item item = createItem();
        Long userId = item.getRequest().getRequestor().getId();
        User user = item.getRequest().getRequestor();
        Long requestId = item.getRequest().getId();
        ItemRequest request = item.getRequest();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.ofNullable(user));
        when(requestRepository.findById(isA(Long.class)))
                .thenReturn(Optional.ofNullable(request));
        when(itemRepository.findAllByRequestId(isA(Long.class)))
                .thenReturn(Collections.singletonList(item));

        ItemRequestDto outputDto = requestService.getById(requestId, userId);
        assertEquals(requestId, outputDto.getId());
        assertEquals(expectedItems, outputDto.getItems().size());
        if (request != null) checkFields(request, outputDto);
        checkFields(item, outputDto);

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findAllByRequestId(isA(Long.class));
    }

    @Test
    void getById_whenValidAndNoItems_thenDtoWithoutItemsReturned() {
        int expectedItems = 0;
        ItemRequest request = createItemRequest();
        Long requestId = request.getId();
        Long userId = request.getRequestor().getId();
        User user = request.getRequestor();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(isA(Long.class)))
                .thenReturn(Collections.emptyList());

        ItemRequestDto outputDto = requestService.getById(requestId, userId);
        assertEquals(requestId, outputDto.getId());
        assertEquals(expectedItems, outputDto.getItems().size());
        checkFields(request, outputDto);

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findAllByRequestId(isA(Long.class));
    }

    @Test
    void getById_whenInvokedByOtherUser_thenDtoReturned() {
        User otherUser = createUser(3L, "Paul", "paul@example.com");
        Long userId = otherUser.getId();
        ItemRequest request = createItemRequest();
        Long requestId = request.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(otherUser));
        when(requestRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(isA(Long.class)))
                .thenReturn(Collections.emptyList());

        ItemRequestDto outputDto = requestService.getById(requestId, userId);
        assertEquals(requestId, outputDto.getId());
        checkFields(request, outputDto);

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findAllByRequestId(isA(Long.class));
    }

    @Test
    void getById_whenUserNotFound_thenEntityNotFoundExceptionReturned() {
        Long userId = 1000L;
        Long requestId = 1L;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> requestService.getById(requestId, userId));
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, never())
                .findById(isA(Long.class));
        verify(itemRepository, never())
                .findAllByRequestId(isA(Long.class));
    }

    @Test
    void getById_whenRequestNotFound_thenEntityNotFoundExceptionReturned() {
        Long requestId = 1000L;
        User user = createRequestor();
        Long userId = user.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> requestService.getById(requestId, userId));
        assertEquals(ItemRequest.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, never())
                .findAllByRequestId(isA(Long.class));
    }

    @Test
    void getAllByRequestorId_whenValidAndRequestsWithItems_thenDtosWithItemsReturned() {
        int expectedRequests = 2;
        int expectedItemsRequest1 = 1;
        int expectedItemsRequest2 = 0;
        Item item = createItem();
        User requestor = item.getRequest().getRequestor();
        Long requestorId = requestor.getId();
        ItemRequest request1 = item.getRequest();
        ItemRequest request2 = createItemRequest(2L, "Item Request 2 Description", requestor);
        List<ItemRequest> requests = new ArrayList<>(Arrays.asList(request1, request2));

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(requestor));
        when(requestRepository.getAllByRequestorIdOrderByCreatedDesc(isA(Long.class)))
                .thenReturn(requests);
        when(itemRepository.findAllByRequestIds(anyList()))
                .thenReturn(Collections.singletonList(item));

        List<ItemRequestDto> outputDtos = requestService.getAllByRequestorId(requestorId);

        assertEquals(expectedRequests, outputDtos.size());
        assertEquals(expectedItemsRequest1, outputDtos.get(0).getItems().size());
        assertEquals(expectedItemsRequest2, outputDtos.get(1).getItems().size());
        checkFields(request1, outputDtos.get(0));
        checkFields(request2, outputDtos.get(1));
        checkFields(item, outputDtos.get(0));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .getAllByRequestorIdOrderByCreatedDesc(isA(Long.class));
        verify(itemRepository, times(1))
                .findAllByRequestIds(anyList());
    }
    
    @Test
    void getAllByRequestorId_whenValidAndRequestsWithoutItems_thenDtosWithoutItemsReturned() {
        int expectedRequests = 2;
        int expectedItems = 0;
        ItemRequest request1 = createItemRequest();
        User requestor = request1.getRequestor();
        Long requestorId = request1.getRequestor().getId();
        ItemRequest request2 = createItemRequest(2L, "Item Request 2 Description", requestor);
        List<ItemRequest> requests = new ArrayList<>(Arrays.asList(request1, request2));

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(requestor));
        when(requestRepository.getAllByRequestorIdOrderByCreatedDesc(isA(Long.class)))
                .thenReturn(requests);
        when(itemRepository.findAllByRequestIds(anyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> outputDtos = requestService.getAllByRequestorId(requestorId);

        assertEquals(expectedRequests, outputDtos.size());
        assertEquals(expectedItems, outputDtos.get(0).getItems().size());
        assertEquals(expectedItems, outputDtos.get(1).getItems().size());
        checkFields(request1, outputDtos.get(0));
        checkFields(request2, outputDtos.get(1));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .getAllByRequestorIdOrderByCreatedDesc(isA(Long.class));
        verify(itemRepository, times(1))
                .findAllByRequestIds(anyList());
    }


    @Test
    void getAllByRequestorId_whenValidAndNoRequests_thenEmptyListReturned() {
        int expectedRequests = 0;
        User requestor = createRequestor();
        Long requestorId = requestor.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(requestor));
        when(requestRepository.getAllByRequestorIdOrderByCreatedDesc(isA(Long.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> outputDtos = requestService.getAllByRequestorId(requestorId);

        assertEquals(expectedRequests, outputDtos.size());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .getAllByRequestorIdOrderByCreatedDesc(isA(Long.class));
        verify(itemRepository, never())
                .findAllByRequestIds(anyList());
    }

    @Test
    void getAllByRequestorId_whenUserNotFound_thenEntityNotFoundExceptionReturned() {
        Long requestorId = 1000L;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> requestService.getAllByRequestorId(requestorId));
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, never())
                .getAllByRequestorIdOrderByCreatedDesc(isA(Long.class));
        verify(itemRepository, never())
                .findAllByRequestIds(anyList());
    }

    @Test
    void getAllByOtherUsers_whenUserNotFound_thenEntityNotFoundExceptionReturned() {
        Long userId = 1000L;

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> requestService.getAllByOtherUsers(userId, DEFAULT_PAGEABLE));
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, never())
                .findAllByOtherUsers(isA(Long.class), isA(Pageable.class));
        verify(itemRepository, never())
                .findAllByRequestIds(anyList());
    }

    @Test
    void getAllByOtherUsers_whenRequestsHaveItems_thenDtosWithItemsReturned() {
        int expectedRequests = 2;
        int expectedItemsRequest1 = 1;
        int expectedItemsRequest2 = 0;

        User user = createUser(3L, "Paul", "paul@example.com");
        Long userId = user.getId();
        Item item = createItem();
        User requestor2 = item.getOwner();
        ItemRequest request1 = item.getRequest();
        ItemRequest request2 = createItemRequest(2L, "Item Request 2 Description", requestor2);
        List<ItemRequest> requests = new ArrayList<>(Arrays.asList(request1, request2));

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(requests);
        when(itemRepository.findAllByRequestIds(anyList()))
                .thenReturn(Collections.singletonList(item));

        List<ItemRequestDto> outputDtos = requestService.getAllByOtherUsers(userId, DEFAULT_PAGEABLE);

        assertEquals(expectedRequests, outputDtos.size());
        assertEquals(expectedItemsRequest1, outputDtos.get(0).getItems().size());
        assertEquals(expectedItemsRequest2, outputDtos.get(1).getItems().size());
        checkFields(request1, outputDtos.get(0));
        checkFields(request2, outputDtos.get(1));
        checkFields(item, outputDtos.get(0));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findAllByOtherUsers(isA(Long.class), isA(Pageable.class));
        verify(itemRepository, times(1))
                .findAllByRequestIds(anyList());
    }

    @Test
    void getAllByOtherUsers_whenRequestsWithNoItems_thenDtosWithNoItemsReturned() {
        int expectedRequests = 2;
        int expectedItems = 0;

        User user = createUser(3L, "Paul", "paul@example.com");
        Long userId = user.getId();
        ItemRequest request1 = createItemRequest();
        User requestor2 = createOwner();
        ItemRequest request2 = createItemRequest(2L, "Item Request 2 Description", requestor2);
        List<ItemRequest> requests = new ArrayList<>(Arrays.asList(request1, request2));

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(requests);
        when(itemRepository.findAllByRequestIds(anyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> outputDtos = requestService.getAllByOtherUsers(userId, DEFAULT_PAGEABLE);

        assertEquals(expectedRequests, outputDtos.size());
        assertEquals(expectedItems, outputDtos.get(0).getItems().size());
        assertEquals(expectedItems, outputDtos.get(1).getItems().size());
        checkFields(request1, outputDtos.get(0));
        checkFields(request2, outputDtos.get(1));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findAllByOtherUsers(isA(Long.class), isA(Pageable.class));
        verify(itemRepository, times(1))
                .findAllByRequestIds(anyList());
    }

    @Test
    void getAllByOtherUsers_whenNoRequests_thenEmptyListReturned() {
        int expectedRequests = 0;

        User user = createUser(3L, "Paul", "paul@example.com");
        Long userId = user.getId();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByOtherUsers(isA(Long.class), isA(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> outputDtos = requestService.getAllByOtherUsers(userId, DEFAULT_PAGEABLE);

        assertEquals(expectedRequests, outputDtos.size());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(requestRepository, times(1))
                .findAllByOtherUsers(isA(Long.class), isA(Pageable.class));
        verify(itemRepository, never())
                .findAllByRequestIds(anyList());
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private void checkFields(ItemRequest request, ItemRequestDto requestDto) {
        assertEquals(request.getId(), requestDto.getId());
        assertEquals(request.getDescription(), requestDto.getDescription());
        assertEquals(request.getCreated(), requestDto.getCreated());
    }

    private void checkFields(Item item, ItemRequestDto requestDto) {
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());
        assertEquals(item.getName(), requestDto.getItems().get(0).getName());
        assertEquals(item.getDescription(), requestDto.getItems().get(0).getDescription());
        assertEquals(item.getIsAvailable(), requestDto.getItems().get(0).getAvailable());
        assertEquals(item.getRequest().getId(), requestDto.getItems().get(0).getRequestId());
    }

    private ItemRequestDto createInputDto(Long requestorId) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setRequestorId(requestorId);
        dto.setDescription("Description Item Request 1");
        return dto;
    }

    private ItemRequest createItemRequest(Long expectedId, ItemRequestDto inputDto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setId(expectedId);
        request.setDescription(inputDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private ItemRequest createItemRequest(Long expectedId, String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setId(expectedId);
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private ItemRequest createItemRequest() {
        User requestor = createRequestor();
        return createItemRequest(1L, "Item Request 1 Description", requestor);
    }

    private Item createItem() {
        User owner = createOwner();
        ItemRequest request = createItemRequest();
        return createItem(
                owner,
                request);
    }

    private Item createItem(User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(1L);
        item.setName("Peter's Item 1");
        item.setDescription("Peter's Item 1 Description");
        item.setIsAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    private User createOwner() {
        return createUser(1L, "Peter", "peter@example.com");
    }

    private User createRequestor() {
        return createUser(2L, "Kate", "kate@example.com");
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
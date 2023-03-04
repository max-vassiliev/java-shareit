package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Spy
    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @Spy
    private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    @Spy
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);


    @Test
    void save_whenValid_thenDtoWithIdReturned() {
        Long expectedId = 1L;
        ItemDto inputDto = createItemDto();
        Item item = createItem();
        User owner = createUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto outputDto = itemService.save(inputDto);

        assertEquals(expectedId, outputDto.getId());
        checkFields(inputDto, outputDto);
    }

    @Test
    void add_whenValidAndContainsRequestId_thenDtoWithRequestIdReturned() {
        Long expectedId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setRequestId(1L);
        Item item = createItem();
        User owner = createUser();
        ItemRequest request = createRequest();
        item.setOwner(owner);
        item.setRequest(request);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        when(requestRepository.findById(any()))
                .thenReturn(Optional.of(request));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto outputDto = itemService.save(inputDto);

        assertEquals(expectedId, outputDto.getId());
        assertEquals(inputDto.getRequestId(), outputDto.getRequestId());
        checkFields(inputDto, outputDto);
    }

    @Test
    void add_whenUserNotFound_thenNotFoundExceptionThrown() {
        ItemDto itemDto = createItemDto();

        when(userRepository.findById(any()))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> itemService.save(itemDto));
    }

    @Test
    void add_whenRequestCreatedByItemOwner_thenValidationExceptionThrown() {
        ItemDto itemDto = createItemDto();
        User user = createUser();
        ItemRequest request = createRequest(user);
        itemDto.setRequestId(request.getId());

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(any()))
                .thenReturn(Optional.of(request));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.save(itemDto));

        assertEquals("Владелец вещи не может отвечать на собственный запрос", exception.getMessage());
    }

    @Test
    void update_whenValid_thenDtoReturned() {
        Long itemId = 1L;
        ItemDto inputDto = createItemDto();
        inputDto.setId(itemId);
        User owner = createUser();
        Item item = createItem();
        item.setOwner(owner);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.update(inputDto);
        assertEquals(outputDto.getId(), inputDto.getId());
        checkFields(inputDto, outputDto);
    }

    @Test
    void update_whenUsedIdNotOwnerId_thenForbiddenExceptionThrown() {
        Long itemId = 1L;
        Long wrongOwnerId = 2L;
        ItemDto inputDto = createItemDto();
        inputDto.setId(itemId);
        inputDto.setOwnerId(wrongOwnerId);
        User owner = createUser();
        Item item = createItem();
        item.setOwner(owner);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> itemService.update(inputDto));
    }

    @Test
    void update_whenAvailableUpdated_thenDtoReturned() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);
        item.setIsAvailable(false);

        ItemDto inputDto = new ItemDto();
        inputDto.setId(item.getId());
        inputDto.setOwnerId(owner.getId());
        inputDto.setAvailable(false);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.update(inputDto);
        assertEquals(outputDto.getId(), inputDto.getId());
        assertEquals(outputDto.getAvailable(), inputDto.getAvailable());
        assertNotNull(outputDto.getName());
        assertNotNull(outputDto.getDescription());
    }

    @Test
    void update_whenDescriptionUpdated_thenDtoReturned() {
        String updatedDescription = "Item 1 Description Update";
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);
        item.setDescription(updatedDescription);

        ItemDto inputDto = new ItemDto();
        inputDto.setId(item.getId());
        inputDto.setOwnerId(owner.getId());
        inputDto.setDescription(updatedDescription);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.update(inputDto);
        assertEquals(outputDto.getId(), inputDto.getId());
        assertEquals(outputDto.getDescription(), inputDto.getDescription());
        assertNotNull(outputDto.getName());
        assertNotNull(outputDto.getAvailable());
    }

    @Test
    void update_whenNameUpdated_thenDtoReturned() {
        String updatedName = "Item 1 Upd";
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);
        item.setName(updatedName);

        ItemDto inputDto = new ItemDto();
        inputDto.setId(item.getId());
        inputDto.setOwnerId(owner.getId());
        inputDto.setName(updatedName);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.update(inputDto);
        assertEquals(outputDto.getId(), inputDto.getId());
        assertEquals(outputDto.getName(), inputDto.getName());
        assertNotNull(outputDto.getDescription());
        assertNotNull(outputDto.getAvailable());
    }

    @Test
    void getByIdAndUserId_whenValid_thenDtoReturned() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);

        Long itemId = item.getId();
        Long ownerId = item.getOwner().getId();

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.getByIdAndUserId(itemId, ownerId);

        assertEquals(itemId, outputDto.getId());
        checkFieldsNotNull(outputDto);
    }



    @Test
    void getByIdAndUserId_whenUserNotOwner_thenDtoReturned() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);

        Long itemId = item.getId();
        Long otherUserId = 2L;

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.getByIdAndUserId(itemId, otherUserId);

        assertEquals(itemId, outputDto.getId());
        checkFieldsNotNull(outputDto);
    }

    @Test
    void getByIdAndUserId_whenItemNotFound_thenNotFoundExceptionThrown() {
        Long itemId = 1L;
        Long userId = 1L;

        when(itemRepository.findById(any()))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> itemService.getByIdAndUserId(itemId, userId));
    }

    @Test
    void getAllByOwnerId_whenValid_thenDtosReturned() {
        Long ownerId = 1L;
        List<Item> items = createItems();
        User owner = items.get(0).getOwner();
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(any(), any()))
                .thenReturn(items);

        List<ItemDto> outputDtos = itemService.getAllByOwnerId(ownerId, defaultPageable);
        assertEquals(outputDtos.size(), items.size());
        checkFields(items, outputDtos);
    }

    
    @Test
    void getAllByKeyword_whenValid_thenDtosReturned() {
        String keyword = "keYwOrd";
        List<Item> allItems = createItems();
        List<Item> relevantItems = new ArrayList<>(Arrays.asList(allItems.get(1), allItems.get(2)));
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        when(itemRepository.searchByKeyword(any(), any()))
                .thenReturn(relevantItems);

        List<ItemDto> outputDtos = itemService.getAllByKeyword(keyword, defaultPageable);
        assertEquals(outputDtos.size(), relevantItems.size());
        checkFields(relevantItems, outputDtos);
    }

    @Test
    void getAllByKeyword_whenValidButItemsNotFound_thenEmptyListReturned() {
        String keyword = "quErY";
        List<Item> foundItems = Collections.emptyList();
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        when(itemRepository.searchByKeyword(any(), any()))
                .thenReturn(foundItems);

        List<ItemDto> output = itemService.getAllByKeyword(keyword, defaultPageable);
        assertEquals(0, output.size());
    }

    @Test
    void getAllByKeyword_whenKeywordIsEmpty_thenEmptyListReturned() {
        String keyword = "";
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        List<ItemDto> output = itemService.getAllByKeyword(keyword, defaultPageable);
        assertEquals(0, output.size());
    }

    @Test
    void getByIdAndUserId_whenInvokedByOwner_thenDtoWithBookingsReturned() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);

        User user2 = new User(2L, "Kate", "kate@example.com");
        User user3 = new User(3L, "Paul", "paul@example.com");

        Booking lastBooking = createBooking(1L, item, user2,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        Booking nextBooking = createBooking(2L, item, user3,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findLastByItemId(any(), any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findNextByItemId(any(), any()))
                .thenReturn(nextBooking);

        ItemDto outputDto = itemService.getByIdAndUserId(item.getId(), owner.getId());

        assertNotNull(outputDto.getLastBooking());
        assertNotNull(outputDto.getNextBooking());
        checkFields(lastBooking, outputDto, true);
        checkFields(nextBooking, outputDto, false);
    }

    @Test
    void getById_whenUserIsNotOwner_thenDtoWithoutBookingsReturned() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);
        Long otherUserId = 2L;

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.getByIdAndUserId(item.getId(), otherUserId);

        assertNull(outputDto.getLastBooking());
        assertNull(outputDto.getNextBooking());
        assertEquals(item.getId(), outputDto.getId());
        assertEquals(item.getName(), outputDto.getName());
    }

    @Test
    void getAllByOwnerId_whenValid_thenDtosWithBookingsReturned() {
        User owner = createUser();
        Item item1 = createItem();
        Item item2 = createItem2();
        item1.setOwner(owner);
        item2.setOwner(owner);
        List<Item> items = new ArrayList<>(Arrays.asList(item1, item2));
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        User user2 = new User(2L, "Kate", "kate@example.com");
        User user3 = new User(3L, "Paul", "paul@example.com");

        Booking lastBookingItem1 = createBooking(1L, item1, user2,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        Booking nextBookingItem1 = createBooking(2L, item1, user3,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(any(), any()))
                .thenReturn(items);
        when(bookingRepository.findLastByItemIds(any(), any()))
                .thenReturn(new ArrayList<>(Collections.singletonList(lastBookingItem1)));
        when(bookingRepository.findNextByItemIds(any(), any()))
                .thenReturn(new ArrayList<>(Collections.singletonList(nextBookingItem1)));

        List<ItemDto> outputDtos = itemService.getAllByOwnerId(owner.getId(), defaultPageable);

        assertEquals(outputDtos.size(), items.size());
        assertNotNull(outputDtos.get(0).getLastBooking());
        assertNotNull(outputDtos.get(0).getNextBooking());
        checkFields(lastBookingItem1, outputDtos.get(0), true);
        checkFields(nextBookingItem1, outputDtos.get(0), false);
    }

    @Test
    void getByIdAndUserId_whenNoComments_thenDtoWithEmptyCommentsListReturned() {
        Long userId = 2L;
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));

        ItemDto outputDto = itemService.getByIdAndUserId(item.getId(), userId);
        assertEquals(0, outputDto.getComments().size());
    }

    @Test
    void saveComment_whenNoPriorBooking_thenValidationExceptionReturned() {
        CommentDto inputDto = createCommentDto();

        when(commentService.saveComment(any()))
                .thenThrow(ValidationException.class);

        assertThrows(ValidationException.class, () -> itemService.saveComment(inputDto));
    }

    @Test
    void saveComment_whenValid_thenDtoReturned() {
        Long expectedId = 1L;
        CommentDto inputDto = createCommentDto();
        CommentDto outputDto = createCommentDtoOut(inputDto.getText());

        when(commentService.saveComment(any())).thenReturn(outputDto);

        CommentDto result = itemService.saveComment(inputDto);

        assertEquals(expectedId, result.getId());
        assertEquals(inputDto.getText(), result.getText());
        assertNotNull(result.getAuthorName());
        assertNotNull(result.getCreated());
    }


    @Test
    void getByIdAndUserId_whenItemHasComments_thenGetCommentsInvoked() {
        Long otherUserId = 2L;
        User owner = createUser();
        Item item = createItem();
        item.setOwner(owner);

        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));
        doNothing().when(commentService).getComments(any());

        itemService.getByIdAndUserId(item.getId(), otherUserId);

        verify(commentService, times(1)).getComments(any());
    }

    @Test
    void getByIdAndUserId_whenInvokedByOwner_thenDtoWithBookingsReturnedAndGetCommentsInvoked() {
        Item item = createItem();
        User owner = createUser();
        item.setOwner(owner);

        User user2 = new User(2L, "Kate", "kate@example.com");
        User user3 = new User(3L, "Paul", "paul@example.com");

        Booking lastBooking = createBooking(1L, item, user2,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        Booking nextBooking = createBooking(2L, item, user3,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        when(itemRepository.findById(any()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findLastByItemId(any(), any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findNextByItemId(any(), any()))
                .thenReturn(nextBooking);
        doNothing().when(commentService).getComments(any());

        ItemDto outputDto = itemService.getByIdAndUserId(item.getId(), owner.getId());

        assertNotNull(outputDto.getLastBooking());
        assertNotNull(outputDto.getNextBooking());
        checkFields(lastBooking, outputDto, true);
        checkFields(nextBooking, outputDto, false);

        verify(commentService, times(1)).getComments(any());
    }


    @Test
    void getAllByOwnerId_whenValid_thenDtosWithBookingsReturnedAndGetCommentsInvoked() {
        User owner = createUser();
        Item item1 = createItem();
        Item item2 = createItem2();
        item1.setOwner(owner);
        item2.setOwner(owner);
        List<Item> items = new ArrayList<>(Arrays.asList(item1, item2));
        Pageable defaultPageable = new CustomPageRequest(0, 10, Sort.by("id"));

        User user2 = new User(2L, "Kate", "kate@example.com");
        User user3 = new User(3L, "Paul", "paul@example.com");

        Booking lastBookingItem1 = createBooking(1L, item1, user2,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        Booking nextBookingItem1 = createBooking(2L, item1, user3,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(any(), any()))
                .thenReturn(items);
        when(bookingRepository.findLastByItemIds(any(), any()))
                .thenReturn(new ArrayList<>(Collections.singletonList(lastBookingItem1)));
        when(bookingRepository.findNextByItemIds(any(), any()))
                .thenReturn(new ArrayList<>(Collections.singletonList(nextBookingItem1)));
        doNothing().when(commentService).getComments(any(), any());

        List<ItemDto> outputDtos = itemService.getAllByOwnerId(owner.getId(), defaultPageable);
        assertEquals(outputDtos.size(), items.size());

        assertNotNull(outputDtos.get(0).getLastBooking());
        assertNotNull(outputDtos.get(0).getNextBooking());
        assertEquals(outputDtos.get(0).getId(), outputDtos.get(0).getLastBooking().getItemId());
        assertEquals(outputDtos.get(0).getId(), outputDtos.get(0).getNextBooking().getItemId());
        checkFields(lastBookingItem1, outputDtos.get(0), true);
        checkFields(nextBookingItem1, outputDtos.get(0), false);

        verify(commentService, times(1)).getComments(any(), any());
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private void checkFields(ItemDto inputDto, ItemDto outputDto) {
        assertEquals(inputDto.getName(), outputDto.getName());
        assertEquals(inputDto.getDescription(), outputDto.getDescription());
        assertEquals(inputDto.getAvailable(), outputDto.getAvailable());
    }

    private void checkFields(List<Item> items, List<ItemDto> itemDtos) {
        for (int i = 0; i < itemDtos.size(); i++) {
            assertEquals(items.get(i).getId(), itemDtos.get(i).getId());
            assertEquals(items.get(i).getName(), itemDtos.get(i).getName());
            assertEquals(items.get(i).getDescription(), itemDtos.get(i).getDescription());
            assertEquals(items.get(i).getIsAvailable(), itemDtos.get(i).getAvailable());
        }
    }

    private void checkFields(Booking booking, ItemDto itemDto, boolean isLastBooking) {
        assertEquals(booking.getItem().getId(), itemDto.getId());
        if (isLastBooking) {
            assertEquals(booking.getId(), itemDto.getLastBooking().getId());
            assertEquals(booking.getItem().getId(), itemDto.getLastBooking().getItemId());
            assertEquals(booking.getBooker().getId(), itemDto.getLastBooking().getBookerId());
        } else {
            assertEquals(booking.getId(), itemDto.getNextBooking().getId());
            assertEquals(booking.getItem().getId(), itemDto.getNextBooking().getItemId());
            assertEquals(booking.getBooker().getId(), itemDto.getNextBooking().getBookerId());
        }
    }

    private void checkFieldsNotNull(ItemDto itemDto) {
        assertNotNull(itemDto.getName());
        assertNotNull(itemDto.getDescription());
        assertNotNull(itemDto.getAvailable());
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setOwnerId(1L);
        itemDto.setAvailable(true);
        return itemDto;
    }

    private Item createItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Peter's Item 1");
        item.setDescription("Peter's Item 1 Description");
        item.setIsAvailable(true);
        return item;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Peter");
        user.setEmail("peter@example.com");
        return user;
    }

    private ItemRequest createRequest() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Item Request 1 Description");
        request.setRequestor(new User(2L, "Kate", "kate@example.com"));
        request.setCreated(LocalDateTime.now().minusDays(1));
        return request;
    }

    private ItemRequest createRequest(User requestor) {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Item Request 1 description");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now().minusDays(1));
        return request;
    }

    private Item createItem2() {
        Item item = new Item();
        item.setId(2L);
        item.setName("Peter's Item 2");
        item.setDescription("Peter's Item 2 Description");
        item.setIsAvailable(true);
        return item;
    }

    private Booking createBooking(Long id, Item item, User booker, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }

    private CommentDto createCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Item 1 Comment");
        commentDto.setAuthorId(2L);
        return commentDto;
    }

    private CommentDto createCommentDtoOut(String text) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText(text);
        commentDto.setAuthorName("Kate");
        commentDto.setCreated(LocalDateTime.now());
        return commentDto;
    }

    private List<Item> createItems() {
        User owner = createUser();

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Peter's Item 1");
        item1.setDescription("Peter's Item 1 Description");
        item1.setIsAvailable(true);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Peter's Item 2 (keyword)");
        item2.setDescription("Peter's Item 2 Description");
        item2.setIsAvailable(true);
        item2.setOwner(owner);

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Peter's Item 3");
        item3.setDescription("Peter's Item 3 Description (keyword)");
        item3.setIsAvailable(true);
        item3.setOwner(owner);

        return new ArrayList<>(Arrays.asList(item1, item2, item3));
    }
}
package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.common.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    CommentServiceImpl commentService;

    @Spy
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);


    @Test
    void saveComment_whenValid_thenCommentDtoReturned() {
        CommentDto inputDto = createInputDto();
        User author = createAuthor();
        Item item = createItem();
        Booking booking = createBooking();
        Comment comment = createComment(inputDto.getText(), item, author, LocalDateTime.now());

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(booking));
        when(commentRepository.save(isA(Comment.class)))
                .thenReturn(comment);

        CommentDto outputDto = commentService.saveComment(inputDto);

        assertEquals(comment.getId(), outputDto.getId());
        assertEquals(comment.getText(), outputDto.getText());
        assertEquals(comment.getAuthor().getName(), outputDto.getAuthorName());
        assertEquals(comment.getCreated(), outputDto.getCreated());
        assertEquals(inputDto.getAuthorId(), author.getId());
        assertEquals(inputDto.getItemId(), item.getId());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findById(isA(Long.class));
        verify(bookingRepository, times(1))
                .findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class));
        verify(commentRepository, times(1))
                .save(isA(Comment.class));
    }

    @Test
    void saveComment_whenNoPriorBookings_thenValidationExceptionThrown() {
        CommentDto inputDto = createInputDto();
        User author = createAuthor();
        Item item = createItem();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class,
                () -> commentService.saveComment(inputDto));

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findById(isA(Long.class));
        verify(bookingRepository, times(1))
                .findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class));
        verify(commentRepository, never())
                .save(isA(Comment.class));
    }

    @Test
    void saveComment_whenAuthorNotFound_thenEntityNotFoundExceptionThrown() {
        CommentDto inputDto = createInputDto();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentService.saveComment(inputDto));
        assertEquals(User.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, never())
                .findById(isA(Long.class));
        verify(bookingRepository, never())
                .findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class));
        verify(commentRepository, never())
                .save(isA(Comment.class));
    }

    @Test
    void saveComment_whenItemNotFound_thenEntityNotFoundExceptionThrown() {
        CommentDto inputDto = createInputDto();
        User author = createAuthor();

        when(userRepository.findById(isA(Long.class)))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(isA(Long.class)))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentService.saveComment(inputDto));
        assertEquals(Item.class, exception.getEntityClass());

        verify(userRepository, times(1))
                .findById(isA(Long.class));
        verify(itemRepository, times(1))
                .findById(isA(Long.class));
        verify(bookingRepository, never())
                .findByBookerAndItemPast(isA(User.class), isA(Item.class), isA(LocalDateTime.class));
        verify(commentRepository, never())
                .save(isA(Comment.class));
    }

    @Test
    void getComments_whenValidAndItemHasComment_thenCommentsFound() {
        ItemDto itemDto = createItemDtoOut();
        Item item = createItem();
        User author = createAuthor();
        Comment comment = createComment("Item 1 Comment", item, author, LocalDateTime.now());

        when(commentRepository.findAllByItem_Id(isA(Long.class)))
                .thenReturn(Collections.singletonList(comment));

        commentService.getComments(itemDto);

        verify(commentRepository, times(1))
                .findAllByItem_Id(isA(Long.class));
    }

    @Test
    void getComments_whenValidAndNoComments_thenNoExceptionThrown() {
        ItemDto itemDto = createItemDtoOut();

        when(commentRepository.findAllByItem_Id(isA(Long.class)))
                .thenReturn(Collections.emptyList());

        commentService.getComments(itemDto);

        verify(commentRepository, times(1))
                .findAllByItem_Id(isA(Long.class));
    }

    @Test
    void getComments_whenValidAndItemsHaveComments_thenCommentsFound() {
        List<ItemDto> itemDtos = createItemDtosOut();
        List<Long> itemIds = itemDtos.stream().map(ItemDto::getId).collect(Collectors.toList());
        Item item = createItem();
        User author = createAuthor();
        Comment comment = createComment("Item 1 Comment", item, author, LocalDateTime.now());

        when(commentRepository.findAllByItemIds(anyList()))
                .thenReturn(Collections.singletonList(comment));

        commentService.getComments(itemDtos, itemIds);

        verify(commentRepository, times(1))
                .findAllByItemIds(anyList());
    }

    @Test
    void getComments_whenValidAndItemsHaveNoComments_thenNoExceptionThrown() {
        List<ItemDto> itemDtos = createItemDtosOut();
        List<Long> itemIds = itemDtos.stream().map(ItemDto::getId).collect(Collectors.toList());

        when(commentRepository.findAllByItemIds(anyList()))
                .thenReturn(Collections.emptyList());

        commentService.getComments(itemDtos, itemIds);

        verify(commentRepository, times(1))
                .findAllByItemIds(anyList());
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private CommentDto createInputDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setAuthorId(2L);
        commentDto.setItemId(1L);
        commentDto.setText("Item 1 Comment");
        return commentDto;
    }


    private ItemDto createItemDtoOut(Long id, String name, String description) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);
        return itemDto;
    }

    private ItemDto createItemDtoOut() {
        return createItemDtoOut(1L,
                "Peter's Item 1",
                "Peter's Item 1 Description"
        );
    }

    private List<ItemDto> createItemDtosOut() {
        ItemDto itemDto1 = createItemDtoOut();
        ItemDto itemDto2 = createItemDtoOut(2L,
                "Peter's Item 2",
                "Peter's Item 2 Description"
        );
        return new ArrayList<>(Arrays.asList(itemDto1, itemDto2));
    }

    private Comment createComment(String text, Item item, User author, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);
        return comment;
    }

    private User createAuthor() {
        return createUser(2L, "Kate", "kate@example.com");
    }

    private User createOwner() {
        return createUser(1L, "Peter", "peter@example.com");
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item createItem() {
        User owner = createOwner();
        return createItem(
                owner);
    }

    private Item createItem(User owner) {
        Item item = new Item();
        item.setId(1L);
        item.setName("Peter's Item 1");
        item.setDescription("Peter's Item 1 Description");
        item.setIsAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private Booking createBooking() {
        User booker = createAuthor();
        Item item = createItem();

        return createBooking(
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                item,
                booker
        );
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end,
                                  Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }
}
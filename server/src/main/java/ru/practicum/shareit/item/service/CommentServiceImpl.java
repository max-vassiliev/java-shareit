package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;


    @Override
    @Transactional
    public CommentDto saveComment(CommentDto commentDto) {
        User author = getUser(commentDto.getAuthorId());
        Item item = getItem(commentDto.getItemId());
        commentDto.setCreated(LocalDateTime.now());
        validateAuthorForComment(author, item, commentDto.getCreated());

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void getComments(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItem_Id(itemDto.getId());

        comments.stream()
                .map(commentMapper::toCommentDto)
                .forEach(commentDto -> itemDto.getComments().add(commentDto));
    }

    @Override
    public void getComments(List<ItemDto> itemDtos, List<Long> itemIds) {
        List<Comment> comments = commentRepository.findAllByItemIds(itemIds);
        if (comments.isEmpty()) return;

        Map<Long, List<CommentDto>> itemComments = new HashMap<>();

        comments.forEach(comment -> itemComments
                .computeIfAbsent(comment.getItem().getId(), (commentList -> new ArrayList<>()))
                .add(commentMapper.toCommentDto(comment)));

        itemDtos.forEach(itemDto -> itemDto.setComments(
                itemComments.getOrDefault(itemDto.getId(), Collections.emptyList())));
    }

    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + userId, User.class
                ));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найдена вещь с ID " + itemId, Item.class
                ));
    }

    private void validateAuthorForComment(User author, Item item, LocalDateTime commentCreated) {
        List<Booking> bookings = bookingRepository.findByBookerAndItemPast(author, item, commentCreated);
        if (bookings.isEmpty()) {
            throw new ValidationException(String.format(
                    "Автор с ID %d еще не брал в аренду вещь с ID %d", author.getId(), item.getId()
            ));
        }
    }
}

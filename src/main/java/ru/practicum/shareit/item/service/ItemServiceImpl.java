package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Override
    public ItemDto getByIdAndUserId(Long itemId, Long userId) {
        Item item = getItem(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        if (Objects.equals(userId, item.getOwner().getId())) {
            addLastAndNextBookings(itemDto, LocalDateTime.now());
        }
        addComments(itemDto);

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId) {
        List<Item> userItems = itemRepository.findByOwnerIdOrderById(ownerId);
        if (userItems == null) return Collections.emptyList();

        List<ItemDto> userItemDtos = userItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        List<Long> itemIds = userItemDtos.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        addLastAndNextBookings(userItemDtos, itemIds, LocalDateTime.now());
        addComments(userItemDtos, itemIds);
        return userItemDtos;
    }

    @Override
    public List<ItemDto> getAllByKeyword(String keyword) {
        if (keyword.isBlank()) return Collections.emptyList();

        return itemRepository.searchByKeyword(keyword.toUpperCase())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());
        User owner = userRepository.findById(itemDto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + itemDto.getOwnerId(), User.class));

        Item item = ItemMapper.toItemCreate(itemDto);
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());
        Item item = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найдена вещь с ID " + itemDto.getId(), Item.class));
        validateOwnerOnUpdate(itemDto, item);
        updateFields(item, itemDto);

        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto saveComment(CommentDto commentDto) {
        User author = getUser(commentDto.getAuthorId());
        Item item = getItem(commentDto.getItemId());
        validateAuthorForComment(author, item, commentDto.getCreated());
        Comment comment = CommentMapper.toCommentCreate(commentDto, item, author);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }


    // ----------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ----------------------

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

    private void validateOwnerId(Long ownerId) {
        if (ownerId <= 0) {
            throw new ValidationException("Идентификатор владельца должен быть положительным числом. " +
                    "Передан ID: " + ownerId);
        }
    }

    private void validateAuthorForComment(User author, Item item, LocalDateTime commentCreated) {
        List<Booking> bookings = bookingRepository.findByBookerAndItemPast(author, item, commentCreated);
        if (bookings.isEmpty()) {
            throw new ValidationException(String.format(
                    "Автор с ID %d еще не брал в аренду вещь с ID %d", author.getId(), item.getId()
            ));
        }
    }

    private void validateOwnerOnUpdate(ItemDto updatedItem, Item item) {
        if (!Objects.equals(updatedItem.getOwnerId(), item.getOwner().getId())) {
            throw new ForbiddenException("Вносить изменения может только владелец вещи");
        }
    }

    private void updateFields(Item item, ItemDto itemDto) {
        if (ItemDto.isNameNotNull(itemDto)) {
            item.setName(itemDto.getName());
        }
        if (ItemDto.isDescriptionNotNull(itemDto)) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setIsAvailable(itemDto.getAvailable());
        }
    }

    private void addLastAndNextBookings(ItemDto itemDto, LocalDateTime now) {
        Booking lastBooking = bookingRepository.findLastByItemId(itemDto.getId(), now);
        Booking nextBooking = bookingRepository.findNextByItemId(itemDto.getId(), now);
        if (lastBooking != null) itemDto.setLastBooking(BookingMapper.toBookingDtoLite(lastBooking));
        if (nextBooking != null) itemDto.setNextBooking(BookingMapper.toBookingDtoLite(nextBooking));
    }

    private void addLastAndNextBookings(List<ItemDto> itemDtos, List<Long> itemIds, LocalDateTime now) {
        List<Booking> lastBookings = bookingRepository.findLastByItemIds(itemIds, now);
        List<Booking> nextBookings = bookingRepository.findNextByItemIds(itemIds, now);

        Map<Long, BookingDto> itemLastBookings = new HashMap<>();
        Map<Long, BookingDto> itemNextBookings = new HashMap<>();

        lastBookings.forEach(booking -> itemLastBookings.put(booking.getItem().getId(),
                BookingMapper.toBookingDtoLite(booking)));

        nextBookings.forEach(booking -> itemNextBookings.put(booking.getItem().getId(),
                BookingMapper.toBookingDtoLite(booking)));

        itemDtos.forEach(itemDto -> {
            itemDto.setLastBooking(itemLastBookings.get(itemDto.getId()));
            itemDto.setNextBooking(itemNextBookings.get(itemDto.getId()));
        });
    }

    private void addComments(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItem_Id(itemDto.getId());
        itemDto.setComments(new ArrayList<>());

        comments.stream()
                .map(CommentMapper::toCommentDto)
                .forEach(commentDto -> itemDto.getComments().add(commentDto));
    }

    private void addComments(List<ItemDto> itemDtos, List<Long> itemIds) {
        List<Comment> comments = commentRepository.findAllByItemIds(itemIds);
        Map<Long, List<CommentDto>> itemComments = new HashMap<>();

        comments.forEach(comment -> itemComments
                .computeIfAbsent(comment.getItem().getId(), (commentList -> new ArrayList<>()))
                .add(CommentMapper.toCommentDto(comment)));
        itemDtos.forEach(itemDto -> itemDto.setComments(itemComments.get(itemDto.getId())));
    }
}

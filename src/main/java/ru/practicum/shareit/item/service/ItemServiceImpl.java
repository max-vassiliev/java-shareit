package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository requestRepository;
    private final CommentService commentService;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;


    @Override
    public ItemDto getByIdAndUserId(Long itemId, Long userId) {
        Item item = getItem(itemId);
        ItemDto itemDto = itemMapper.toItemDto(item);
        if (Objects.equals(userId, item.getOwner().getId())) {
            addLastAndNextBookings(itemDto, LocalDateTime.now());
        }
        commentService.getComments(itemDto);

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId, Pageable pageable) {
        getUser(ownerId);
        List<Item> userItems = itemRepository.findByOwnerId(ownerId, pageable);
        if (userItems == null) return Collections.emptyList();

        List<ItemDto> userItemDtos = userItems.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
        List<Long> itemIds = userItemDtos.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        addLastAndNextBookings(userItemDtos, itemIds, LocalDateTime.now());
        commentService.getComments(userItemDtos, itemIds);
        return userItemDtos;
    }

    @Override
    public List<ItemDto> getAllByKeyword(String keyword, Pageable pageable) {
        if (keyword.isEmpty()) return Collections.emptyList();

        return itemRepository.searchByKeyword(keyword, pageable)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto) {
        User owner = getUser(itemDto.getOwnerId());
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        setItemRequest(itemDto, item);

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto) {
        Item item = getItem(itemDto.getId());
        validateOwnerOnUpdate(itemDto, item);
        updateFields(item, itemDto);

        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto saveComment(CommentDto commentDto) {
        return commentService.saveComment(commentDto);
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найдена вещь с ID " + itemId, Item.class
                ));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + userId, User.class
                ));
    }

    private void setItemRequest(ItemDto itemDto, Item item) {
        if (itemDto.getRequestId() == null) return;

        ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден запрос с ID " + itemDto.getRequestId(), ItemRequest.class
                ));
        if (itemDto.getOwnerId().equals(request.getRequestor().getId())) {
            throw new ValidationException("Владелец вещи не может отвечать на собственный запрос");
        }

        item.setRequest(request);
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
        if (lastBooking != null) itemDto.setLastBooking(bookingMapper.toBookingDtoLite(lastBooking));
        if (nextBooking != null) itemDto.setNextBooking(bookingMapper.toBookingDtoLite(nextBooking));
    }

    private void addLastAndNextBookings(List<ItemDto> itemDtos, List<Long> itemIds, LocalDateTime now) {
        List<Booking> lastBookings = bookingRepository.findLastByItemIds(itemIds, now);
        List<Booking> nextBookings = bookingRepository.findNextByItemIds(itemIds, now);

        Map<Long, BookingDto> itemLastBookings = new HashMap<>();
        Map<Long, BookingDto> itemNextBookings = new HashMap<>();

        lastBookings.forEach(booking -> itemLastBookings.put(booking.getItem().getId(),
                bookingMapper.toBookingDtoLite(booking)));

        nextBookings.forEach(booking -> itemNextBookings.put(booking.getItem().getId(),
                bookingMapper.toBookingDtoLite(booking)));

        itemDtos.forEach(itemDto -> {
            itemDto.setLastBooking(itemLastBookings.get(itemDto.getId()));
            itemDto.setNextBooking(itemNextBookings.get(itemDto.getId()));
        });
    }
}

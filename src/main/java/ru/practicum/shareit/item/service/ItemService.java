package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getByIdAndUserId(Long itemId, Long userId);

    List<ItemDto> getAllByOwnerId(Long ownerId);

    List<ItemDto> getAllByKeyword(String keyword);

    ItemDto save(ItemDto itemDto);

    ItemDto update(ItemDto itemUpdateDto);

    CommentDto saveComment(CommentDto commentDto);
}

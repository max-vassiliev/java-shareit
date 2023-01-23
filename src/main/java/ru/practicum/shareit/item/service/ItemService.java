package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getById(Long itemId);

    List<ItemDto> getAllByOwnerId(Long ownerId);

    List<ItemDto> getAllByKeyword(String keyword);

    ItemDto save(ItemDto itemDto);

    ItemDto update(ItemDto itemUpdateDto);
}

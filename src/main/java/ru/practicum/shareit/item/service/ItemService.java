package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {

    ItemDto getItem(Long itemId);

    List<ItemDto> getUserItems(Long ownerId);

    List<ItemDto> getItemsByKeyword(String keyword);

    ItemDto saveItem(ItemCreateDto itemDto);

    ItemDto updateItem(ItemUpdateDto itemUpdateDto);
}

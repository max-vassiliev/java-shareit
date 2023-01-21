package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item saveItem(Item item);

    Item updateItem(Item itemUpdate);

    Item findItemById(Long itemId);

    List<Item> findUserItems(Long ownerId);

    List<Item> findItemsByKeyword(String keyword);

}

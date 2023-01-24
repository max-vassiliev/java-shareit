package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item save(Item item);

    Item update(Item itemUpdate);

    Item findById(Long itemId);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> findAllByKeyword(String keyword);

}

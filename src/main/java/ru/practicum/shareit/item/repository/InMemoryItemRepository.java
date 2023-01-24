package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {

    private Long nextId = 1L;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> userItemIndex = new LinkedHashMap<>();

    @Override
    public Item findById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new ValidationException("Не найдена вещь с ID " + itemId);
        }
        return item;
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return userItemIndex.get(ownerId);
    }

    @Override
    public List<Item> findAllByKeyword(String keyword) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> (item.getName().toLowerCase().contains(keyword) ||
                        item.getDescription().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
    }

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);

        final List<Item> userItems = userItemIndex.computeIfAbsent(item.getOwnerId(), k -> new ArrayList<>());
        userItems.add(item);

        return item;
    }

    @Override
    public Item update(Item updatedItem) {
        Item item = findById(updatedItem.getId());

        if (Item.isNameNotNull(updatedItem)) {
            item.setName(updatedItem.getName());
        }
        if (Item.isDescriptionNotNull(updatedItem)) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }

        return item;
    }
}

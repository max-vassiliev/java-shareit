package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {

    private Long nextId = 1L;
    private final Map<Long, Item> items = new HashMap<>();

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
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
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

        return item;
    }

    @Override
    public Item update(Item updatedItem) {
        Item item = findById(updatedItem.getId());

        validateOwnerOnUpdate(updatedItem, item);

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


    // ----------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ----------------------

    private void validateOwnerOnUpdate(Item updatedItem, Item item) {
        if (!Objects.equals(updatedItem.getOwnerId(), item.getOwner().getId())) {
            throw new ForbiddenException("Вносить изменения может только владелец вещи");
        }
    }
}

package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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
    private final UserRepository userRepository;

    @Override
    public Item findItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new ValidationException("Не найдена вещь с ID " + itemId);
        }
        return item;
    }

    @Override
    public List<Item> findUserItems(Long ownerId) {
        User owner = userRepository.findUserById(ownerId);

        if (owner.getUserItems().isEmpty()) return null;

        return owner.getUserItems().stream()
                .map(this::findItemById)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemsByKeyword(String keyword) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> (item.getName().toLowerCase().contains(keyword) ||
                        item.getDescription().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
    }

    @Override
    public Item saveItem(Item item) {
        User owner = userRepository.findUserById(item.getOwnerId());

        item.setId(nextId++);
        item.setOwner(owner);
        owner.getUserItems().add(item.getId());
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item updateItem(Item updatedItem) {
        Item item = findItemById(updatedItem.getId());

        validateOwnerOnUpdate(updatedItem, item);

        if (updatedItem.isNameNotNull(updatedItem)) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.isDescriptionNotNull(updatedItem)) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }

        items.put(item.getId(), item);
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

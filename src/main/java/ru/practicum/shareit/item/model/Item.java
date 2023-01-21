package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Item {

    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private Long ownerId;
    private User owner;
    private Long requestId;
    private ItemRequest request;

    // при создании
    public Item(String name, String description, Boolean available, Long ownerId) {
        this.name = name;
        this.description = description;
        this.available = Objects.requireNonNullElse(available, true);
        this.ownerId = ownerId;
    }

    // при обновлении
    public Item(Long id, String name, String description, Boolean available, Long ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.ownerId = ownerId;
    }

    public boolean isNameNotNull(Item item) {
        return item.getName() != null && !item.getName().isBlank();
    }

    public boolean isDescriptionNotNull(Item item) {
        return item.getDescription() != null && !item.getDescription().isBlank();
    }
}

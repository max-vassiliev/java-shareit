package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.findById(itemId));
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId) {
        List<Item> userItems = itemRepository.findAllByOwnerId(ownerId);
        if (userItems == null) return Collections.emptyList();

        return userItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getAllByKeyword(String keyword) {
        if (keyword.isBlank()) return Collections.emptyList();

        return itemRepository.findAllByKeyword(keyword.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto save(ItemDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());
        User owner = userRepository.getById(itemDto.getOwnerId());

        Item item = ItemMapper.toItemCreate(itemDto);
        item.setOwnerId(owner.getId());
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(ItemDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());

        Item item = itemRepository.findById(itemDto.getId());
        validateOwnerOnUpdate(itemDto, item);

        Item itemUpdate = ItemMapper.toItemUpdate(itemDto);
        itemUpdate.setOwnerId(itemDto.getOwnerId());

        return ItemMapper.toItemDto(itemRepository.update(itemUpdate));
    }

    // ----------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ----------------------

    private void validateOwnerId(Long ownerId) {
        if (ownerId <= 0) {
            throw new ValidationException("Идентификатор владельца должен быть положительным числом. " +
                    "Передан ID: " + ownerId);
        }
    }

    private void validateOwnerOnUpdate(ItemDto updatedItem, Item item) {
        if (!Objects.equals(updatedItem.getOwnerId(), item.getOwner().getId())) {
            throw new ForbiddenException("Вносить изменения может только владелец вещи");
        }
    }
}

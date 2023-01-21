package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDto getItem(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.findItemById(itemId));
    }

    @Override
    public List<ItemDto> getUserItems(Long ownerId) {
        List<Item> userItems = itemRepository.findUserItems(ownerId);
        if (userItems == null) return Collections.emptyList();

        return userItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByKeyword(String keyword) {
        if (isKeywordNull(keyword)) return Collections.emptyList();

        return itemRepository.findItemsByKeyword(keyword.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto saveItem(ItemCreateDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());
        Item item = ItemMapper.toItem(itemDto);

        return ItemMapper.toItemDto(itemRepository.saveItem(item));
    }

    @Override
    public ItemDto updateItem(ItemUpdateDto itemDto) {
        validateOwnerId(itemDto.getOwnerId());
        Item itemUpdate = ItemMapper.toItem(itemDto);
        itemUpdate.setOwnerId(itemDto.getOwnerId());

        return ItemMapper.toItemDto(itemRepository.updateItem(itemUpdate));
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

    private boolean isKeywordNull(String keyword) {
        return keyword == null || keyword.isBlank();
    }
}

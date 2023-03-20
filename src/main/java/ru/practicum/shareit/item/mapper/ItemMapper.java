package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isAvailable", source = "available")
    @Mapping(target = "request", ignore = true)
    Item toItem(ItemDto itemDto);

    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "available", source = "isAvailable")
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    ItemDto toItemDto(Item item);

    @Mapping(target = "available", source = "isAvailable")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "requestId", source = "request.id")
    ItemItemRequestDto toItemItemRequestDto(Item item);
}

package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isAvailable", source = "available")
    Item toItem(ItemDto itemDto);

    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "available", source = "isAvailable")
    ItemDto toItemDto(Item item);

}

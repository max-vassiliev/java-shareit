package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestor", ignore = true)
    @Mapping(target = "created", ignore = true)
    ItemRequest toItemRequest(ItemRequestDto dto);

    @Mapping(target = "requestorId", source = "requestor.id")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

}

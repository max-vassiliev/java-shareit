package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface CommentService {

    CommentDto saveComment(CommentDto commentDto);

    void getComments(ItemDto itemDto);

    void getComments(List<ItemDto> itemDtos, List<Long> itemIds);

}

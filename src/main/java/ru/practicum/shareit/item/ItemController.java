package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";


    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                           @PathVariable Long itemId) {
        log.info("GET /items/{}", itemId);
        return itemService.getByIdAndUserId(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("GET /items | ownerId: {}", ownerId);
        return itemService.getAllByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getAllByKeyword(@RequestParam String text) {
        log.info("GET /items | Keyword: {}", text);
        return itemService.getAllByKeyword(text);
    }

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_HEADER) Long ownerId,
                       @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("POST /items | ownerId: {} | itemDto: {}", ownerId, itemDto);
        itemDto.setOwnerId(ownerId);
        return itemService.save(itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long authorId,
                                 @PathVariable Long itemId,
                                 @Validated(Create.class) @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment | authorId: {} | commentDto: {}", itemId, authorId, commentDto);
        commentDto.setAuthorId(authorId);
        commentDto.setItemId(itemId);
        commentDto.setCreated(LocalDateTime.now());
        return itemService.saveComment(commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("PATCH /items | ownerId: {} | itemId: {} | itemDto: {}", ownerId, itemId, itemDto);
        itemDto.setId(itemId);
        itemDto.setOwnerId(ownerId);
        return itemService.update(itemDto);
    }
}

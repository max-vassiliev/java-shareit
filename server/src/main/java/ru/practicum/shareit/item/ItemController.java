package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

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
    public List<ItemDto> getAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items?from={}&size={} | ownerId: {}", from, size, ownerId);
        return itemService.getAllByOwnerId(ownerId,
                new CustomPageRequest(from, size, Sort.by("id")));
    }

    @GetMapping("/search")
    public List<ItemDto> getAllByKeyword(@RequestParam(name = "text") String text,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items/search?text={}&from={}&size={}", text, from, size);
        return itemService.getAllByKeyword(text,
                new CustomPageRequest(from, size, Sort.by("id")));
    }

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_HEADER) Long ownerId,
                       @RequestBody ItemDto itemDto) {
        log.info("POST /items | ownerId: {} | itemDto: {}", ownerId, itemDto);
        itemDto.setOwnerId(ownerId);
        return itemService.save(itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long authorId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment | authorId: {} | commentDto: {}", itemId, authorId, commentDto);
        commentDto.setAuthorId(authorId);
        commentDto.setItemId(itemId);
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

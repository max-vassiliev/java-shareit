package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@Slf4j
public class ItemController {

    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";


    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                  @PathVariable Long itemId) {
        log.info("GET /items/{}", itemId);
        return itemClient.getByIdAndUserId(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items?from={}&size={} | ownerId: {}", from, size, ownerId);
        return itemClient.getAllByOwnerId(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getAllByKeyword(@RequestParam(name = "text") String text,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items/search?text={}&from={}&size={}", text, from, size);
        return itemClient.getAllByKeyword(text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> add(@Positive @RequestHeader(USER_ID_HEADER) Long ownerId,
                       @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("POST /items | ownerId: {} | itemDto: {}", ownerId, itemDto);
        return itemClient.save(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Positive @RequestHeader(USER_ID_HEADER) Long authorId,
                                 @PathVariable Long itemId,
                                 @Validated(Create.class) @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment | authorId: {} | commentDto: {}", itemId, authorId, commentDto);
        return itemClient.saveComment(authorId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Positive @RequestHeader(USER_ID_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("PATCH /items | ownerId: {} | itemId: {} | itemDto: {}", ownerId, itemId, itemDto);
        return itemClient.update(ownerId, itemId, itemDto);
    }
}

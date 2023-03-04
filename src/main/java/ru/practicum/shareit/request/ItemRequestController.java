package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.common.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@Slf4j
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService requestService;

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@Positive @RequestHeader(USER_ID_HEADER) Long userId,
                                  @Positive @PathVariable Long requestId) {
        log.info("GET /requests/{} | userId: {}", requestId, userId);
        return requestService.getById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequestorId(@Positive @RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("GET /requests | requestorId: {} ", requestorId);
        return requestService.getAllByRequestorId(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllByOtherUsers(@Positive @RequestHeader(USER_ID_HEADER) Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /requests/all?from={}&size={} | userId = {}", from, size, userId);
        return requestService.getAllByOtherUsers(userId,
                new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "created")));
    }

    @PostMapping
    public ItemRequestDto save(@Positive @RequestHeader(USER_ID_HEADER) Long requestorId,
                               @Validated(Create.class) @RequestBody ItemRequestDto requestDto) {
        log.info("POST /requests | requestorId: {} | requestDto: {}", requestorId, requestDto);
        requestDto.setRequestorId(requestorId);
        return requestService.save(requestDto);
    }

}

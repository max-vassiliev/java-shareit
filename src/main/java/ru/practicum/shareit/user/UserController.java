package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.common.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /users?from={}&size={}", from, size);
        return userService.getAll(new CustomPageRequest(from, size, Sort.by("id")));
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("GET /users/{}", id);
        return userService.getById(id);
    }

    @PostMapping
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("POST /users | userDto: {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("PATCH /users | userId: {} | userDto: {}", userId, userDto);
        userDto.setId(userId);
        return userService.update(userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE /users/{}", id);
        userService.delete(id);
    }
}

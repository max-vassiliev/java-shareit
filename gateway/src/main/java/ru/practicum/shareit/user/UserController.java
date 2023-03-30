package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.common.Update;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@Slf4j
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /users?from={}&size={}", from, size);
        return userClient.getAll(from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        log.info("GET /users/{}", id);
        return userClient.getById(id);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("POST /users | userDto: {}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId,
                                         @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("PATCH /users | userId: {} | userDto: {}", userId, userDto);
        return userClient.update(userId, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("DELETE /users/{}", id);
        return userClient.delete(id);
    }
}

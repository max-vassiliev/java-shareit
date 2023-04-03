package ru.practicum.shareit.user.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    @SneakyThrows
    void serializeFromUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Peter");
        userDto.setEmail("peter@example.com");

        JsonContent<UserDto> result = jacksonTester.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Peter");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("peter@example.com");
    }

    @Test
    @SneakyThrows
    void deserializeToUserDto() {
        String json = "{" + "\"id\": 1, \"name\": \"Peter\", \"email\": \"peter@example.com\"}";

        UserDto userDto = jacksonTester.parse(json).getObject();

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("Peter");
        assertThat(userDto.getEmail()).isEqualTo("peter@example.com");
    }
}
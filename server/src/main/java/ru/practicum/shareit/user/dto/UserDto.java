package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    private Long id;

    private String name;

    private String email;


    public static boolean isNameNotNull(UserDto userDto) {
        return userDto.getName() != null && !userDto.getName().isEmpty();
    }

    public static boolean isEmailNotNull(UserDto userDto) {
        return userDto.getEmail() != null && !userDto.getEmail().isEmpty();
    }
}

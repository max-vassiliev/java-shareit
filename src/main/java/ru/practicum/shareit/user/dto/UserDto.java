package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Укажите имя или логин")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Укажите адрес электронной почты")
    @Email(groups = {Create.class, Update.class}, message = "Проверьте написание адреса электронной почты")
    private String email;

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static boolean isNameNotNull(UserDto userDto) {
        return userDto.getName() != null && !userDto.getName().isBlank();
    }

    public static boolean isEmailNotNull(UserDto userDto) {
        return userDto.getEmail() != null && !userDto.getEmail().isBlank();
    }
}

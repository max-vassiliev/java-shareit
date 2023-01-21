package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {

    private Long id;

    @NotBlank(message = "Укажите имя или логин")
    private String name;

    @NotBlank(message = "Укажите адрес электронной почты")
    @Email(message = "Проверьте написание адреса электронной почты")
    private String email;

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

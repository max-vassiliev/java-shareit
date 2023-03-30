package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.common.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Укажите имя или логин")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Укажите адрес электронной почты")
    @Email(groups = {Create.class, Update.class}, message = "Проверьте написание адреса электронной почты")
    private String email;

}

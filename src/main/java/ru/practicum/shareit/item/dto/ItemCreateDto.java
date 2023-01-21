package ru.practicum.shareit.item.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ItemCreateDto {

    @NotBlank(message = "Необходимо указать название вещи")
    private String name;

    @NotBlank(message = "Необходимо добавить описание")
    private String description;

    @NotNull(message = "Укажите, доступна ли вещь")
    private Boolean available;

    private Long ownerId;
}

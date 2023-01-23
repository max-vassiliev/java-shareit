package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.util.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ItemDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Необходимо указать название вещи")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Необходимо добавить описание")
    private String description;

    @NotNull(groups = {Create.class}, message = "Укажите, доступна ли вещь")
    private Boolean available;

    @JsonIgnore
    private Long ownerId;

    private Long requestId;


    public ItemDto(Long id, String name, String description, boolean available, Long ownerId, Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.ownerId = ownerId;
        this.requestId = requestId;
    }
}

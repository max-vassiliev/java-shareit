package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.common.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemRequestDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Расскажите о том, что вы ищете")
    private String description;

    @NotNull
    private Long requestorId;

    private LocalDateTime created;

    private List<ItemItemRequestDto> items = new ArrayList<>();

}

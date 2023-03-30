package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;

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

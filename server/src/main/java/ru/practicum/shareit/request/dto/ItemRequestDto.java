package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;

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

    private String description;

    private Long requestorId;

    private LocalDateTime created;

    private List<ItemItemRequestDto> items = new ArrayList<>();

}

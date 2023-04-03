package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ItemItemRequestDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long ownerId;

    private Long requestId;

}

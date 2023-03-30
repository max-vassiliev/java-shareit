package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long ownerId;

    private BookingDto lastBooking;

    private BookingDto nextBooking;

    private Long requestId;

    private List<CommentDto> comments = new ArrayList<>();


    public static boolean isNameNotNull(ItemDto itemDto) {
        return itemDto.getName() != null && !itemDto.getName().isEmpty();
    }

    public static boolean isDescriptionNotNull(ItemDto itemDto) {
        return itemDto.getDescription() != null && !itemDto.getDescription().isEmpty();
    }
}

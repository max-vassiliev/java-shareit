package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BookingDto {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;

    private String itemName;

    private ItemDto item;

    private Long bookerId;

    private UserDto booker;

    private BookingState status;

}

package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.common.Create;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BookingDto {

    private Long id;

    @FutureOrPresent(groups = {Create.class}, message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @Future(groups = {Create.class}, message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;

    @NotNull(groups = {Create.class}, message = "Укажите, какую вещь хотите забронировать")
    private Long itemId;

    private String itemName;

    private ItemDto item;

    private Long bookerId;

    private UserDto booker;

    private BookingState status;

}

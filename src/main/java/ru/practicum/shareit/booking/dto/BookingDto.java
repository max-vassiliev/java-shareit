package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Create;

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


    public BookingDto(Long id, LocalDateTime start, LocalDateTime end,
                      Long itemId, String itemName, Long bookerId, BookingState status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.itemId = itemId;
        this.itemName = itemName;
        this.bookerId = bookerId;
        this.status = status;
    }

    public BookingDto(Long id, LocalDateTime start, LocalDateTime end,
                       Item item, User booker, BookingState status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.item = ItemMapper.toItemDto(item);
        this.booker = UserMapper.toUserDto(booker);
        this.status = status;
    }
}

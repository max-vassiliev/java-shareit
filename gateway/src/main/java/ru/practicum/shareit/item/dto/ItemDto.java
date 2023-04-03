package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.common.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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

    private BookingDto lastBooking;

    private BookingDto nextBooking;

    private Long requestId;

    private List<CommentDto> comments = new ArrayList<>();

}

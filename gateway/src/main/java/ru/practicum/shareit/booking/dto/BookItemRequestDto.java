package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

	private long itemId;

	@NotNull(message = "Укажите дату начала бронирования")
	@FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
	private LocalDateTime start;

	@NotNull(message = "Укажите дату окончания бронирования")
	@Future(message = "Дата окончания бронирования должна быть в будущем")
	private LocalDateTime end;
}

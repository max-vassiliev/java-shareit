package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.common.exception.BookingStatusException;

public enum BookingState {

	ALL, 	  // Все
	CURRENT,  // Текущие
	FUTURE,   // Будущие
	PAST, 	  // Завершенные
	REJECTED, // Отклоненные
	APPROVED,  // Подтержденные
	WAITING;  // Ожидающие подтверждения


	public static BookingState fromString(String stringState) {
		try {
			return BookingState.valueOf(stringState.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new BookingStatusException("Unknown state: " + stringState);
		}
	}
}

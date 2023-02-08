package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.error.exception.BookingStatusException;

public enum BookingStateDto {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED,
    CURRENT,
    PAST,
    FUTURE,
    ALL;

    public static BookingStateDto fromString(String string) {
        try {
            return BookingStateDto.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BookingStatusException("Unknown state: " + string);
        }
    }
}

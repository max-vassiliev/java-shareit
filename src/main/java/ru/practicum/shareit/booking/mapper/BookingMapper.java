package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "start", source = "start", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "end", source = "end", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", defaultValue = "WAITING")
    Booking toBooking(BookingDto bookingDto);

    @Mapping(target = "start", source = "start", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "end", source = "end", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "start", source = "start", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "end", source = "end", dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "item", ignore = true)
    BookingDto toBookingDtoLite(Booking booking);

}

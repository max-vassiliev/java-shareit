package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> jacksonTester;

    @Test
    @SneakyThrows
    void serializeFromBookingDto() {
        BookingDto bookingDto = createBookingDto();

        JsonContent<BookingDto> output = jacksonTester.write(bookingDto);

        assertThat(output).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(output).extractingJsonPathStringValue("$.itemName")
                .isEqualTo("Peter's Item 1");
        assertThat(output).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
        assertThat(output).extractingJsonPathMapValue("$.item")
                .extracting("id").isEqualTo(1);
        assertThat(output).extractingJsonPathMapValue("$.item")
                .extracting("name").isEqualTo("Peter's Item 1");
        assertThat(output).extractingJsonPathMapValue("$.item")
                .extracting("description").isEqualTo("Peter's Item 1 Description");
        assertThat(output).extractingJsonPathMapValue("$.item")
                .extracting("available").isEqualTo(true);
        assertThat(output).extractingJsonPathMapValue("$.booker")
                .extracting("id").isEqualTo(2);
        assertThat(output).extractingJsonPathMapValue("$.booker")
                .extracting("name").isEqualTo("Kate");
        assertThat(output).extractingJsonPathMapValue("$.booker")
                .extracting("email").isEqualTo("kate@example.com");
    }

    @Test
    @SneakyThrows
    void deserializeToBookingDto() {
        String json = "{\"start\": \"2024-03-05T14:00:00.000Z\", " +
                "\"end\": \"2024-03-07T14:00:00.000Z\", " +
                "\"itemId\": 1}";

        BookingDto bookingDto = jacksonTester.parse(json).getObject();

        assertThat(bookingDto.getItemId()).isEqualTo(1L);
        assertThat(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .isEqualTo("2024-03-05T14:00:00");
        assertThat(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .isEqualTo("2024-03-07T14:00:00");
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private BookingDto createBookingDto() {
        UserDto bookerDto = createBookerDto();
        ItemDto itemDto = createItemDto();

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(3));
        bookingDto.setItemId(itemDto.getId());
        bookingDto.setItemName(itemDto.getName());
        bookingDto.setItem(itemDto);
        bookingDto.setBookerId(bookerDto.getId());
        bookingDto.setBooker(bookerDto);
        bookingDto.setStatus(BookingState.APPROVED);

        return bookingDto;
    }

    private UserDto createBookerDto() {
        UserDto bookerDto = new UserDto();
        bookerDto.setId(2L);
        bookerDto.setName("Kate");
        bookerDto.setEmail("kate@example.com");
        return bookerDto;
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setAvailable(true);
        return itemDto;
    }
}
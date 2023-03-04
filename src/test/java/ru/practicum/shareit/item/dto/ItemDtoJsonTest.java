package ru.practicum.shareit.item.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> jacksonTester;


    @Test
    @SneakyThrows
    void serializeToItemDto() {
        ItemDto itemDto = createItemDto();

        JsonContent<ItemDto> output = jacksonTester.write(itemDto);

        assertThat(output).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.name")
                .isEqualTo("Peter's Item 1");
        assertThat(output).extractingJsonPathStringValue("$.description")
                .isEqualTo("Peter's Item 1 Description");
        assertThat(output).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(true);
        assertThat(output).extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(1);
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("id").isEqualTo(1);
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("start")
                .isEqualTo(itemDto.getLastBooking().getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("end")
                .isEqualTo(itemDto.getLastBooking().getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("itemId").isEqualTo(1);
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("bookerId").isEqualTo(2);
        assertThat(output).extractingJsonPathMapValue("$.lastBooking")
                .extracting("status").isEqualTo("APPROVED");
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("id").isEqualTo(2);
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("start")
                .isEqualTo(itemDto.getNextBooking().getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("end")
                .isEqualTo(itemDto.getNextBooking().getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("itemId").isEqualTo(1);
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("bookerId").isEqualTo(3);
        assertThat(output).extractingJsonPathMapValue("$.nextBooking")
                .extracting("status").isEqualTo("APPROVED");
        assertThat(output).extractingJsonPathArrayValue("$.comments")
                .hasSize(1);
        assertThat(output).extractingJsonPathNumberValue("$.comments[0].id")
                .isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.comments[0].text")
                .isEqualTo("Item 1 Comment");
        assertThat(output).extractingJsonPathNumberValue("$.comments[0].authorId")
                .isEqualTo(2);
        assertThat(output).extractingJsonPathStringValue("$.comments[0].authorName")
                .isEqualTo("Kate");
        assertThat(output).extractingJsonPathStringValue("$.comments[0].created")
                .isEqualTo(itemDto.getComments().get(0).getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    @SneakyThrows
    void deserializeFromItemDto() {
        String json = "{\"name\": \"Peter's Item 1\", " +
                "\"description\": \"Peter's Item 1 Description\", " +
                "\"available\": true, " +
                "\"requestId\": 1 }";

        ItemDto itemDto = jacksonTester.parse(json).getObject();

        assertThat(itemDto.getName()).isEqualTo("Peter's Item 1");
        assertThat(itemDto.getDescription()).isEqualTo("Peter's Item 1 Description");
        assertThat(itemDto.getAvailable()).isEqualTo(true);
        assertThat(itemDto.getRequestId()).isEqualTo(1L);
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private ItemDto createItemDto() {
        BookingDto lastBooking = createLastBooking();
        BookingDto nextBooking = createNextBooking();
        CommentDto comment1 = createCommentDto();

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Peter's Item 1");
        itemDto.setDescription("Peter's Item 1 Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L);
        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);
        itemDto.getComments().add(comment1);

        return itemDto;
    }

    private BookingDto createLastBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(3));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        bookingDto.setItemId(1L);
        bookingDto.setItemName("Peter's Item 1");
        bookingDto.setBookerId(2L);
        bookingDto.setStatus(BookingState.APPROVED);

        return bookingDto;
    }

    private BookingDto createNextBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(2L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(3));
        bookingDto.setItemId(1L);
        bookingDto.setItemName("Peter's Item 1");
        bookingDto.setBookerId(3L);
        bookingDto.setStatus(BookingState.APPROVED);

        return bookingDto;
    }

    private CommentDto createCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Item 1 Comment");
        commentDto.setItemId(1L);
        commentDto.setAuthorId(2L);
        commentDto.setAuthorName("Kate");
        commentDto.setCreated(LocalDateTime.now());
        return commentDto;
    }
}
package ru.practicum.shareit.request.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> jacksonTester;

    @Test
    @SneakyThrows
    void serializeFromItemRequestDto() {
        ItemRequestDto requestDto = createItemRequestDto();

        JsonContent<ItemRequestDto> output = jacksonTester.write(requestDto);

        assertThat(output).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.description")
                .isEqualTo("Item Request 1 Description");
        assertThat(output).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(output).extractingJsonPathStringValue("$.created")
                .isEqualTo(requestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(output).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(output).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(output).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Item 1");
        assertThat(output).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("Item 1 Description");
        assertThat(output).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(output).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(1);
        assertThat(output).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
    }

    @Test
    @SneakyThrows
    void deserializeToItemRequestDto() {
        String json = "{\"description\": \"Item Request 1\"}";

        ItemRequestDto requestDto = jacksonTester.parse(json).getObject();

        assertThat(requestDto.getDescription()).isEqualTo("Item Request 1");
    }


    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private ItemRequestDto createItemRequestDto() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Item Request 1 Description");
        requestDto.setRequestorId(2L);
        requestDto.setCreated(LocalDateTime.now());
        requestDto.getItems().add(createItemDto());
        return requestDto;
    }

    private ItemItemRequestDto createItemDto() {
        ItemItemRequestDto itemDto = new ItemItemRequestDto();
        itemDto.setId(1L);
        itemDto.setName("Item 1");
        itemDto.setDescription("Item 1 Description");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(1L);
        return itemDto;
    }
}
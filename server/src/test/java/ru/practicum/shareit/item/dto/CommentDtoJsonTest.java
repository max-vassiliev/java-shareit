package ru.practicum.shareit.item.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> jacksonTester;

    @Test
    @SneakyThrows
    void serializeFromCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Item 1 Comment");
        commentDto.setItemId(1L);
        commentDto.setAuthorId(2L);
        commentDto.setAuthorName("Kate");
        commentDto.setCreated(LocalDateTime.now());

        JsonContent<CommentDto> result = jacksonTester.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Item 1 Comment");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.authorId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Kate");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(commentDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    @SneakyThrows
    void deserializeToCommentDto() {
        String json = "{\"text\": \"Item 1 Comment\"}";

        CommentDto commentDto = jacksonTester.parse(json).getObject();

        assertThat(commentDto.getText()).isEqualTo("Item 1 Comment");
    }
}
package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.common.Create;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CommentDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Напишите комментарий")
    private String text;

    private Long itemId;

    private Long authorId;

    private String authorName;

    private LocalDateTime created;

}

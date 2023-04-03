package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CommentDto {

    private Long id;

    private String text;

    private Long itemId;

    private Long authorId;

    private String authorName;

    private LocalDateTime created;

}

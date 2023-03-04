package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItem_Id(Long itemId);

    @Query("select c from Comment c " +
            "where c.item.id in :itemIds")
    List<Comment> findAllByItemIds(@Param("itemIds") List<Long> itemIds);

}

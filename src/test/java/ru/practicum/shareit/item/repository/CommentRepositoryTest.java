package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private Item item1;

    private Item item2;

    private Comment comment1;

    private Comment comment2;

    private Comment comment3;


    @BeforeEach
    void setUp() {
        User owner = userRepository.save(createUser("Peter", "peter@example.com"));
        User author1 = userRepository.save(createUser("Kate", "kate@example.com"));
        User author2 = userRepository.save(createUser("Paul", "paul@example.com"));

        item1 = itemRepository.save(createItem(owner, "Peter's Item 1",
                "Peter's Item 1 Description"));
        item2 = itemRepository.save(createItem(owner, "Peter's Item 2",
                "Peter's Item 2 Description"));

        comment1 = commentRepository.save(createComment("Item 1 Comment By Kate",
                item1, author1, LocalDateTime.now().minusDays(3)));
        comment2 = commentRepository.save(createComment("Item 1 Comment By Paul",
                item1, author2, LocalDateTime.now().minusDays(2)));
        comment3 = commentRepository.save(createComment("Item 2 Comment By Kate",
                item2, author1, LocalDateTime.now().minusDays(1)));
    }

    @AfterEach
    void deleteAll() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void findAllByItem_Id_whenCommentsExist_thenCommentsListReturned() {
        int expectedComments = 2;
        Long itemId = item1.getId();

        List<Comment> comments = commentRepository.findAllByItem_Id(itemId);

        assertEquals(expectedComments, comments.size());
        assertEquals(comment1.getItem(), item1);
        assertEquals(comment2.getItem(), item1);
        assertTrue(comments.contains(comment1));
        assertTrue(comments.contains(comment2));
        assertNotEquals(comment3.getItem(), item1);
        assertFalse(comments.contains(comment3));
    }

    @Test
    void findAllByItem_Id_whenNoComments_thenEmptyListReturned() {
        int expectedComments = 0;
        Long itemId = item1.getId();
        commentRepository.deleteAll();

        List<Comment> comments = commentRepository.findAllByItem_Id(itemId);

        assertEquals(expectedComments, comments.size());
    }

    @Test
    void findAllByItemIds_whenCommentsExist_thenCommentsListReturned() {
        int expectedComments = 3;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));

        List<Comment> comments = commentRepository.findAllByItemIds(itemIds);

        assertEquals(expectedComments, comments.size());
        assertEquals(comment1.getItem(), item1);
        assertEquals(comment2.getItem(), item1);
        assertEquals(comment3.getItem(), item2);
        assertTrue(comments.contains(comment1));
        assertTrue(comments.contains(comment2));
        assertTrue(comments.contains(comment3));
    }

    @Test
    void findAllByItemIds_whenNoComments_thenEmptyListReturned() {
        int expectedComments = 0;
        List<Long> itemIds = new ArrayList<>(Arrays.asList(item1.getId(), item2.getId()));
        commentRepository.deleteAll();

        List<Comment> comments = commentRepository.findAllByItemIds(itemIds);

        assertEquals(expectedComments, comments.size());
    }

    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private Comment createComment(String text, Item item, User author, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);
        return comment;
    }

    private Item createItem(User owner, String name, String description) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
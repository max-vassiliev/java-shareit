package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("select item from Item item " +
            "where item.isAvailable = true " +
            "and (upper(item.name) like upper(concat('%', ?1, '%')) " +
            "or upper(item.description) like upper(concat('%', ?1, '%')))")
    List<Item> searchByKeyword(String keyword, Pageable pageable);

    @Query("select item from Item item " +
            "where item.isAvailable = true " +
            "and item.request.id = ?1 ")
    List<Item> findAllByRequestId(Long requestId);

    @Query("select item from Item item " +
            "where item.isAvailable = true " +
            "and item.request.id in :requestIds")
    List<Item> findAllByRequestIds(@Param("requestIds") List<Long> requestIds);
}

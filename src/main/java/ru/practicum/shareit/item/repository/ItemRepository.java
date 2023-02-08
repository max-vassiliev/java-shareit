package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByOwnerIdOrderById(Long ownerId);

    @Query("select item from Item as item " +
            "where item.isAvailable = true " +
            "and (upper(item.name) like %:keyword% " +
            "or upper(item.description) like %:keyword%)")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

}

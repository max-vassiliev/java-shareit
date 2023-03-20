package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> getAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    @Query("select ir from ItemRequest ir " +
            "where ir.requestor.id <> :requestorId ")
    List<ItemRequest> findAllByOtherUsers(@Param("requestorId") Long requestorId, Pageable pageable);

}

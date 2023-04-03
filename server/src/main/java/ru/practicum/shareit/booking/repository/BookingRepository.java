package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking as b " +
            "where b.booker = :booker " +
            "and b.item = :item " +
            "and b.start < :now " +
            "and b.status = 'APPROVED'")
    List<Booking> findByBookerAndItemPast(@Param("booker") User booker,
                                          @Param("item") Item item,
                                          @Param("now") LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.booker = ?1")
    List<Booking> findAllByBooker(User booker, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.status = 'WAITING'")
    List<Booking> findAllByBookerWaiting(User booker, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.status = 'REJECTED'")
    List<Booking> findAllByBookerRejected(User booker, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.end <= ?2 ")
    List<Booking> findAllByBookerPast(User booker, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.start <= ?2 " +
            "and b.end > ?2 ")
    List<Booking> findAllByBookerCurrent(User booker, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.start > ?2 ")
    List<Booking> findAllByBookerFuture(User booker, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true)")
    List<Booking> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.item in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true) " +
            "and b.status = 'WAITING'")
    List<Booking> findByOwnerIdWaiting(Long ownerId, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.item in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true) " +
            "and b.status = 'REJECTED'")
    List<Booking> findByOwnerIdRejected(Long ownerId, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.item in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true) " +
            "and b.end <= ?2 ")
    List<Booking> findByOwnerIdPast(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.item in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true) " +
            "and b.start <= ?2 " +
            "and b.end > ?2 ")
    List<Booking> findByOwnerIdCurrent(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking as b " +
            "where b.item in " +
            "(select item.id from Item item " +
            "where item.owner.id = ?1 " +
            "and item.isAvailable = true) " +
            "and b.start > ?2 ")
    List<Booking> findByOwnerIdFuture(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select booking from Booking booking " +
            "where booking.start in " +
            "(select max(b.start) " +
            "from Booking as b " +
            "where b.item.id in :itemId " +
            "and b.start <= :now " +
            "and b.status = 'APPROVED') " +
            "and booking.item.id in :itemId")
    Booking findLastByItemId(@Param("itemId") Long itemId,
                             @Param("now") LocalDateTime now);

    @Query(nativeQuery = true,
            value = "select * from bookings b " +
                    "inner join (" +
                    "select item_id, max(starts) as last_start from bookings " +
                    "where starts <= :now " +
                    "and item_id in :itemIds " +
                    "and status = 'APPROVED' " +
                    "group by item_id" +
                    ") last_bookings " +
                    "on b.item_id = last_bookings.item_id " +
                    "and b.starts = last_start " +
                    "where b.id = " +
                    "(select max(id) from bookings " +
                    "where item_id = b.item_id " +
                    "and starts = b.starts)")
    List<Booking> findLastByItemIds(@Param("itemIds") List<Long> itemIds,
                                    @Param("now") LocalDateTime now);

    @Query("select booking from Booking booking " +
            "where booking.start in " +
            "(select min(b.start) " +
            "from Booking as b " +
            "where b.item.id in :itemId " +
            "and b.start > :now " +
            "and b.status = 'APPROVED') " +
            "and booking.item.id in :itemId")
    Booking findNextByItemId(@Param("itemId") Long itemId,
                             @Param("now") LocalDateTime now);

    @Query(nativeQuery = true,
            value = "select * from bookings b " +
                    "inner join (" +
                    "select item_id, min(starts) as next_start from bookings " +
                    "where starts > :now " +
                    "and item_id in :itemIds " +
                    "and status = 'APPROVED'" +
                    "group by item_id" +
                    ") next_bookings " +
                    "on b.item_id = next_bookings.item_id " +
                    "and b.starts = next_start " +
                    "where b.id = " +
                    "(select max(id) from bookings " +
                    "where item_id = b.item_id " +
                    "and starts = b.starts)")
    List<Booking> findNextByItemIds(@Param("itemIds") List<Long> itemIds,
                                    @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = :itemId " +
            "and (b.start = :starts " +
            "or b.end = :ends " +
            "or (b.start between :starts and :ends) " +
            "or (b.end between :starts and :ends) " +
            "or (:starts between b.start and b.end) " +
            "or (:ends between b.start and b.end))")
    List<Booking> findOverlaps(@Param("itemId") Long itemId,
                               @Param("starts") LocalDateTime starts,
                               @Param("ends") LocalDateTime ends);
}

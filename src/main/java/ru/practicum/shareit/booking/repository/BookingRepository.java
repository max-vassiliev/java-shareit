package ru.practicum.shareit.booking.repository;

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
            "where b.booker = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByBooker(User booker);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.status = 'WAITING' " +
            "order by b.start desc")
    List<Booking> findAllByBookerWaiting(User booker);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.status = 'REJECTED' " +
            "order by b.start desc")
    List<Booking> findAllByBookerRejected(User booker);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerPast(User booker, LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerCurrent(User booker, LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.booker = ?1 " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerFuture(User booker, LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItems(@Param("ownerItems") List<Item> ownerItems);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "and b.status = 'WAITING' " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItemsWaiting(@Param("ownerItems") List<Item> ownerItems);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "and b.status = 'REJECTED' " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItemsRejected(@Param("ownerItems") List<Item> ownerItems);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "and b.end < :now " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItemsPast(@Param("ownerItems") List<Item> ownerItems,
                                          @Param("now") LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "and b.start < :now " +
            "and b.end > :now " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItemsCurrent(@Param("ownerItems") List<Item> ownerItems,
                                             @Param("now") LocalDateTime now);

    @Query("select b from Booking as b " +
            "where b.item in :ownerItems " +
            "and b.start > :now " +
            "order by b.start desc")
    List<Booking> findAllByOwnerItemsFuture(@Param("ownerItems") List<Item> ownerItems,
                                            @Param("now") LocalDateTime now);

    @Query("select booking from Booking booking " +
            "where booking.start in " +
            "(select max(b.start) " +
            "from Booking as b " +
            "where b.item.id in :itemId " +
            "and b.start <= :now) " +
            "and booking.item.id in :itemId")
    Booking findLastByItemId(@Param("itemId") Long itemId,
                             @Param("now") LocalDateTime now);

    @Query(nativeQuery = true, value = "select * " +
            "from bookings as b " +
            "where b.item_id in :itemIds " +
            "and b.starts <= :now " +
            "group by b.id " +
            "having b.starts = max(b.starts)")
    List<Booking> findLastByItemIds(@Param("itemIds") List<Long> itemIds,
                                    @Param("now") LocalDateTime now);

    @Query("select booking from Booking booking " +
            "where booking.start in " +
            "(select min(b.start) " +
            "from Booking as b " +
            "where b.item.id in :itemId " +
            "and b.start > :now) " +
            "and booking.item.id in :itemId")
    Booking findNextByItemId(@Param("itemId") Long itemId,
                             @Param("now") LocalDateTime now);

    @Query(nativeQuery = true, value = "select * " +
            "from bookings as b " +
            "where b.item_id in :itemIds " +
            "and b.starts > :now " +
            "group by b.id " +
            "having b.starts = min(b.starts)")
    List<Booking> findNextByItemIds(@Param("itemIds") List<Long> itemIds,
                                    @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = :itemId " +
            "and (b.start = :starts " +
            "or b.end = :ends " +
            "or (b.start between :starts and :ends) " +
            "or (b.end between :starts and :ends))")
    List<Booking> findOverlaps(@Param("itemId") Long itemId,
                               @Param("starts") LocalDateTime starts,
                               @Param("ends") LocalDateTime ends);
}

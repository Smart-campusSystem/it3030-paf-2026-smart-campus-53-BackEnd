package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Booking;
import com.smart_campus_system.demo.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select (count(b) > 0)
            from Booking b
            where b.resource.id = :resourceId
              and b.status in (com.smart_campus_system.demo.model.BookingStatus.PENDING,
                               com.smart_campus_system.demo.model.BookingStatus.APPROVED)
              and b.startTime < :newEnd
              and b.endTime > :newStart
              and (:excludeId is null or b.id <> :excludeId)
            """)
    boolean existsConflictingBooking(@Param("resourceId") Long resourceId,
                                     @Param("newStart") LocalDateTime newStart,
                                     @Param("newEnd") LocalDateTime newEnd,
                                     @Param("excludeId") Long excludeBookingId);

    @Query("""
            select b
            from Booking b
            join b.user u
            where lower(u.email) = lower(:email)
            order by b.createdAt desc
            """)
    List<Booking> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("""
            select b
            from Booking b
            join b.user u
            where lower(u.email) = lower(:email)
              and (:status is null or b.status = :status)
              and (:resourceId is null or b.resource.id = :resourceId)
              and (:fromStart is null or b.startTime >= :fromStart)
              and (:toEnd is null or b.startTime < :toEnd)
            order by b.createdAt desc
            """)
    List<Booking> findForUserWithFilters(@Param("email") String email,
                                         @Param("status") BookingStatus status,
                                         @Param("resourceId") Long resourceId,
                                         @Param("fromStart") LocalDateTime fromStart,
                                         @Param("toEnd") LocalDateTime toEnd);

    @Query("""
            select b
            from Booking b
            where (:status is null or b.status = :status)
              and (:resourceId is null or b.resource.id = :resourceId)
              and (:fromStart is null or b.startTime >= :fromStart)
              and (:toEnd is null or b.startTime < :toEnd)
            order by b.createdAt desc
            """)
    List<Booking> findAdminWithFilters(@Param("status") BookingStatus status,
                                       @Param("resourceId") Long resourceId,
                                       @Param("fromStart") LocalDateTime fromStart,
                                       @Param("toEnd") LocalDateTime toEnd);

    @Query("""
            select b
            from Booking b
            where (:status is null or b.status = :status)
              and (:date is null or (b.startTime >= :startOfDay and b.startTime < :endOfDay))
            order by b.createdAt desc
            """)
    List<Booking> findAllWithFilters(@Param("status") BookingStatus status,
                                     @Param("date") LocalDate date,
                                     @Param("startOfDay") LocalDateTime startOfDay,
                                     @Param("endOfDay") LocalDateTime endOfDay);

    long countByStatus(BookingStatus status);

    @Query("select count(b) from Booking b")
    long countAllBookings();

    @Query("""
            select b.resource.name, count(b)
            from Booking b
            where b.status = com.smart_campus_system.demo.model.BookingStatus.APPROVED
            group by b.resource.id, b.resource.name
            order by count(b) desc
            """)
    List<Object[]> findMostBookedResource();

    @Query(value = """
        SELECT r.id, r.name, COUNT(*) AS cnt
        FROM bookings b
        JOIN resources r ON r.id = b.resource_id
        WHERE b.status = 'APPROVED'
        GROUP BY r.id, r.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
List<Object[]> findTopBookedResources(Pageable pageable);

@Query(value = """
        SELECT HOUR(b.start_time) AS hr, COUNT(*) AS cnt
        FROM bookings b
        WHERE b.status = 'APPROVED'
        GROUP BY HOUR(b.start_time)
        ORDER BY cnt DESC
        """, nativeQuery = true)
List<Object[]> findPeakBookingHours(Pageable pageable);

    @Query("""
            select b
            from Booking b
            where b.resource.id = :resourceId
              and b.status in (com.smart_campus_system.demo.model.BookingStatus.PENDING,
                               com.smart_campus_system.demo.model.BookingStatus.APPROVED)
              and b.startTime < :newEnd
              and b.endTime > :newStart
              and (:excludeId is null or b.id <> :excludeId)
            order by b.startTime asc
            """)
    List<Booking> findConflictingBookings(@Param("resourceId") Long resourceId,
                                          @Param("newStart") LocalDateTime newStart,
                                          @Param("newEnd") LocalDateTime newEnd,
                                          @Param("excludeId") Long excludeBookingId);

    @Query("""
            select count(b)
            from Booking b
            where b.createdAt >= :startOfDay and b.createdAt < :endOfDay
            """)
    long countCreatedBetween(@Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
            select b
            from Booking b
            join fetch b.resource
            join fetch b.user
            where b.id = :id
            """)
    Optional<Booking> findByIdWithJoins(@Param("id") Long id);
}

package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Booking;
import com.smart_campus_system.demo.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select (count(b) > 0)
            from Booking b
            where b.resource.id = :resourceId
              and b.status = com.smart_campus_system.demo.model.BookingStatus.APPROVED
              and b.startTime < :newEnd
              and b.endTime > :newStart
            """)
    boolean existsConflictingBooking(@Param("resourceId") Long resourceId,
                                     @Param("newStart") LocalDateTime newStart,
                                     @Param("newEnd") LocalDateTime newEnd);

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
            where (:status is null or b.status = :status)
              and (:date is null or (b.startTime >= :startOfDay and b.startTime < :endOfDay))
            order by b.createdAt desc
            """)
    List<Booking> findAllWithFilters(@Param("status") BookingStatus status,
                                     @Param("date") LocalDate date,
                                     @Param("startOfDay") LocalDateTime startOfDay,
                                     @Param("endOfDay") LocalDateTime endOfDay);

    long countByStatus(BookingStatus status);

    @Query("""
            select b.resource.name, count(b)
            from Booking b
            where b.status = com.smart_campus_system.demo.model.BookingStatus.APPROVED
            group by b.resource.id, b.resource.name
            order by count(b) desc
            """)
    List<Object[]> findMostBookedResource();

    @Query("""
            select b
            from Booking b
            where b.resource.id = :resourceId
              and b.status = com.smart_campus_system.demo.model.BookingStatus.APPROVED
              and b.startTime < :newEnd
              and b.endTime > :newStart
            order by b.startTime asc
            """)
    List<Booking> findConflictingBookings(@Param("resourceId") Long resourceId,
                                          @Param("newStart") LocalDateTime newStart,
                                          @Param("newEnd") LocalDateTime newEnd);

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

package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUserIdOrderByCheckInDesc(Long userId);
    List<Booking> findAllByPropertyOwnerIdOrderByCheckInDesc(Long ownerId);

    @Query("""
            select (count(b) > 0)
            from Booking b
            where b.property.id = :propertyId
              and :checkIn < b.checkOut
              and :checkOut > b.checkIn
            """)
    boolean existsOverlappingBooking(
            @Param("propertyId") Long propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}

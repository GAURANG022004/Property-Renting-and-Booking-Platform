package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.AvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, Long> {
    List<AvailabilityBlock> findAllByPropertyOwnerId(Long ownerId);

    @Query("""
            select (count(a) > 0) from AvailabilityBlock a
            where a.property.id = :propertyId
              and :checkIn <= a.endDate and :checkOut >= a.startDate
            """)
    boolean existsOverlappingBlock(@Param("propertyId") Long propertyId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);
}

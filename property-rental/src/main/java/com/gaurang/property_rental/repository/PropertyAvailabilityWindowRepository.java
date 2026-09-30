package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.PropertyAvailabilityWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PropertyAvailabilityWindowRepository extends JpaRepository<PropertyAvailabilityWindow, Long> {
    List<PropertyAvailabilityWindow> findAllByPropertyIdOrderByStartDateAsc(Long propertyId);
    List<PropertyAvailabilityWindow> findAllByPropertyOwnerIdOrderByStartDateAsc(Long ownerId);

    @Query("""
            select (count(w) > 0) from PropertyAvailabilityWindow w
            where w.property.id = :propertyId
              and :startDate < w.endDate
              and :endDate > w.startDate
            """)
    boolean existsOverlappingWindow(@Param("propertyId") Long propertyId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
}
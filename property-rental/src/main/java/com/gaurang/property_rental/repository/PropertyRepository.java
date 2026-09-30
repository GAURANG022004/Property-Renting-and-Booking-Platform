package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLocationContainingIgnoreCase(String location);
    List<Property> findAllByOwnerId(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Property p where p.id = :propertyId")
    Optional<Property> findByIdForUpdate(@Param("propertyId") Long propertyId);
}

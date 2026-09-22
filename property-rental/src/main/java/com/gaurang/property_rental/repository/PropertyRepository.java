package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLocationContainingIgnoreCase(String location);
    List<Property> findAllByOwnerId(Long ownerId);
}

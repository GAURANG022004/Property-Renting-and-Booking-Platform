package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    List<PropertyImage> findByPropertyId(Long propertyId);

    @Modifying
    @Transactional
    void deleteByPropertyId(Long propertyId);
}

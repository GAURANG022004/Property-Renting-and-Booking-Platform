package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.PropertyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyCategoryRepository extends JpaRepository<PropertyCategory, Long> { }

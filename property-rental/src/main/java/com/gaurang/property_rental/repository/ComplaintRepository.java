package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> { }

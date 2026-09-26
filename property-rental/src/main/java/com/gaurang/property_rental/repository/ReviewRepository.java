package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByPropertyIdOrderByReviewDateDesc(Long propertyId);
    List<Review> findAllByUserIdOrderByReviewDateDesc(Long userId);
    boolean existsByBookingId(Long bookingId);
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
}
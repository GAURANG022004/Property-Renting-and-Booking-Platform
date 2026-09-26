package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.ReviewCreateRequest;
import com.gaurang.property_rental.dto.ReviewResponse;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.Review;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.ReviewRepository;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only review your own bookings");
        }

        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only review completed bookings");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this booking");
        }

        Property property = booking.getProperty();

        Review review = new Review(
                property,
                user,
                booking,
                request.rating(),
                request.comment()
        );

        Review saved = reviewRepository.save(review);
        updatePropertyRating(property);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getPropertyReviews(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        return reviewRepository.findAllByPropertyIdOrderByReviewDateDesc(propertyId).stream()
                .filter(Review::isApproved)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return reviewRepository.findAllByUserIdOrderByReviewDateDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }

        Property property = review.getProperty();
        reviewRepository.delete(review);
        updatePropertyRating(property);
    }

    private void updatePropertyRating(Property property) {
        List<Review> reviews = reviewRepository.findAllByPropertyIdOrderByReviewDateDesc(property.getId());
        if (reviews.isEmpty()) {
            property.setRating(0.0);
        } else {
            double averageRating = reviews.stream()
                    .filter(Review::isApproved)
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            property.setRating(averageRating);
        }
        propertyRepository.save(property);
    }

    private ReviewResponse toResponse(Review review) {
        Property property = review.getProperty();
        User user = review.getUser();

        return new ReviewResponse(
                review.getId(),
                property.getId(),
                property.getTitle(),
                review.getBooking().getId(),
                user.getFirstName() + " " + user.getLastName(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate(),
                review.isApproved()
        );
    }
}
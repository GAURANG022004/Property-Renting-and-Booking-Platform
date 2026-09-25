package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.ReviewCreateRequest;
import com.gaurang.property_rental.dto.ReviewResponse;
import com.gaurang.property_rental.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewResponse createReview(@Valid @RequestBody ReviewCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return reviewService.createReview(request, userEmail);
    }

    @GetMapping("/property/{propertyId}")
    public List<ReviewResponse> getPropertyReviews(@PathVariable Long propertyId) {
        return reviewService.getPropertyReviews(propertyId);
    }

    @GetMapping("/my-reviews")
    public List<ReviewResponse> getMyReviews() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return reviewService.getUserReviews(userEmail);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        reviewService.deleteReview(id, userEmail);
    }
}
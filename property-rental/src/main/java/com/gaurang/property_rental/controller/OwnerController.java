package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.dto.ReviewResponse;
import com.gaurang.property_rental.dto.owner.*;
import com.gaurang.property_rental.service.ReviewService;
import com.gaurang.property_rental.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/owner")
public class OwnerController {
    private final OwnerService ownerService;
    private final ReviewService reviewService;
    public OwnerController(OwnerService ownerService, ReviewService reviewService) {
        this.ownerService = ownerService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard") 
    public OwnerDashboardResponse dashboard() {
         return ownerService.dashboard(email()); 
    }

    @GetMapping("/properties") 
    public List<PropertyResponse> properties() { 
        return ownerService.properties(email()); 
    }

    @GetMapping("/reviews")
    public List<ReviewResponse> reviews() {
        return reviewService.getOwnerReviews(email());
    }

    @PutMapping("/reviews/{id}/response")
    public ReviewResponse respondToReview(
            @PathVariable Long id,
            @Valid @RequestBody OwnerReviewResponseRequest request
    ) {
        return reviewService.respondToReview(id, request, email());
    }
    
    @PutMapping("/properties/{id}") public PropertyResponse updateProperty(@PathVariable Long id, @Valid @RequestBody OwnerPropertyUpdateRequest request) { return ownerService.updateProperty(id, request, email()); }
    @PatchMapping("/properties/{id}/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivateProperty(@PathVariable Long id) { ownerService.deactivateProperty(id, email()); }
    @PatchMapping("/properties/{id}/activate") public PropertyResponse activateProperty(@PathVariable Long id) { return ownerService.activateProperty(id, email()); }

    @GetMapping("/bookings") public List<BookingResponse> bookings() { return ownerService.bookings(email()); }
    @PatchMapping("/bookings/{id}") public BookingResponse decideBooking(@PathVariable Long id, @Valid @RequestBody BookingDecisionRequest request) { return ownerService.decideBooking(id, request, email()); }
    @PatchMapping("/bookings/{id}/checkin") public BookingResponse checkIn(@PathVariable Long id) { return ownerService.checkIn(id, email()); }
    
    @PatchMapping("/bookings/{id}/checkout") 
    public BookingResponse completeCheckout(@PathVariable Long id) {
         return ownerService.completeCheckout(id, email()); 
    }

    @GetMapping("/availability") public List<AvailabilityBlockResponse> availability() { return ownerService.availability(email()); }
    @PostMapping("/availability") @ResponseStatus(HttpStatus.CREATED) public AvailabilityBlockResponse blockAvailability(@Valid @RequestBody AvailabilityBlockRequest request) { return ownerService.blockAvailability(request, email()); }
    @DeleteMapping("/availability/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void removeAvailability(@PathVariable Long id) { ownerService.removeAvailabilityBlock(id, email()); }

    @GetMapping("/availability-windows") public List<AvailabilityWindowResponse> availabilityWindows() { return ownerService.availabilityWindows(email()); }
    @PostMapping("/availability-windows") @ResponseStatus(HttpStatus.CREATED) public AvailabilityWindowResponse addAvailabilityWindow(@Valid @RequestBody AvailabilityWindowRequest request) { return ownerService.addAvailabilityWindow(request, email()); }
    @DeleteMapping("/availability-windows/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void removeAvailabilityWindow(@PathVariable Long id) { ownerService.removeAvailabilityWindow(id, email()); }

    private String email() { return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }
}

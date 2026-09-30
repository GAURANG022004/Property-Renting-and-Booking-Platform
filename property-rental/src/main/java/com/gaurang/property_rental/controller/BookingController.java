package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.BookingCreateRequest;
import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.AvailabilityCheckRequest;
import com.gaurang.property_rental.dto.AvailabilityCheckResponse;
import com.gaurang.property_rental.dto.CancellationQuoteResponse;
import com.gaurang.property_rental.dto.CancellationRequest;
import com.gaurang.property_rental.dto.CancellationResponse;
import com.gaurang.property_rental.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();

        return bookingService.create(
                request.propertyId(),
                request.checkIn(),
                request.checkOut(),
                request.guestCount(),
                userEmail
        );
    }

    @PostMapping("/availability-check")
    public AvailabilityCheckResponse checkAvailability(@Valid @RequestBody AvailabilityCheckRequest request) {
        return bookingService.checkAvailability(request);
    }

    @GetMapping
    public List<BookingResponse> myBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return bookingService.history(userEmail);
    }

    @GetMapping("/{id}/cancellation-quote")
    public CancellationQuoteResponse cancellationQuote(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return bookingService.cancellationQuote(id, userEmail);
    }

    @PatchMapping("/{id}/cancel")
    public CancellationResponse cancel(@PathVariable Long id, @Valid @RequestBody CancellationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return bookingService.cancel(id, request.acknowledged(), userEmail);
    }
}


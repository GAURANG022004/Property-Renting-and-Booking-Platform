package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.BookingCreateRequest;
import com.gaurang.property_rental.dto.BookingResponse;
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
                userEmail
        );
    }

    @GetMapping
    public List<BookingResponse> myBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return bookingService.history(userEmail);
    }
}


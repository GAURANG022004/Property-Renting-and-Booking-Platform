package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.BookingCreateRequest;
import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.UserRepository;
import com.gaurang.property_rental.repository.AvailabilityBlockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;

    public BookingService(
            BookingRepository bookingRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            AvailabilityBlockRepository availabilityBlockRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
    }

    @Transactional
    public BookingResponse create(Long propertyId, LocalDate checkIn, LocalDate checkOut, String userEmail) {
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (!"APPROVED".equals(property.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }

        if (availabilityBlockRepository.existsOverlappingBlock(propertyId, checkIn, checkOut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This property is unavailable for the selected dates");
        }

        boolean overlaps = bookingRepository.existsOverlappingBooking(propertyId, checkIn, checkOut);
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This property is already booked for the selected dates");
        }

        Booking saved = bookingRepository.save(new Booking(checkIn, checkOut, property, user));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> history(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return bookingRepository.findAllByUserIdOrderByCheckInDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking b) {
        Property p = b.getProperty();
        return new BookingResponse(
                b.getId(),
                p.getId(),
                p.getTitle(),
                p.getLocation(),
                b.getCheckIn(),
                b.getCheckOut(),
                b.getStatus()
        );
    }
}

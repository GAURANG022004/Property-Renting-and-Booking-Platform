package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.dto.owner.*;
import com.gaurang.property_rental.model.AvailabilityBlock;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.AvailabilityBlockRepository;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyImageRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OwnerService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository imageRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityBlockRepository availabilityRepository;

    public OwnerService(UserRepository userRepository, PropertyRepository propertyRepository,
                        PropertyImageRepository imageRepository, BookingRepository bookingRepository,
                        AvailabilityBlockRepository availabilityRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.imageRepository = imageRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> properties(String email) {
        return propertyRepository.findAllByOwnerId(owner(email).getId()).stream().map(this::toProperty).toList();
    }

    @Transactional
    public PropertyResponse updateProperty(Long propertyId, OwnerPropertyUpdateRequest request, String email) {
        Property property = ownedProperty(propertyId, email);
        property.update(request.title().trim(), request.description().trim(), request.location().trim(), request.pricePerNight());
        return toProperty(property);
    }

    @Transactional
    public void deactivateProperty(Long propertyId, String email) {
        ownedProperty(propertyId, email).deactivate();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> bookings(String email) {
        return bookingRepository.findAllByPropertyOwnerIdOrderByCheckInDesc(owner(email).getId()).stream().map(this::toBooking).toList();
    }

    @Transactional
    public BookingResponse decideBooking(Long bookingId, BookingDecisionRequest request, String email) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> notFound("Booking"));
        if (!booking.getProperty().getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property's booking");
        }
        if (!"PENDING".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending bookings can be accepted or rejected");
        }
        booking.decide(request.status());
        return toBooking(booking);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlockResponse> availability(String email) {
        return availabilityRepository.findAllByPropertyOwnerId(owner(email).getId()).stream().map(this::toAvailability).toList();
    }

    @Transactional
    public AvailabilityBlockResponse blockAvailability(AvailabilityBlockRequest request, String email) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be after startDate");
        }
        Property property = ownedProperty(request.propertyId(), email);
        AvailabilityBlock block = availabilityRepository.save(new AvailabilityBlock(property, request.startDate(), request.endDate(), request.reason()));
        return toAvailability(block);
    }

    @Transactional
    public void removeAvailabilityBlock(Long id, String email) {
        AvailabilityBlock block = availabilityRepository.findById(id).orElseThrow(() -> notFound("Availability block"));
        if (!block.getProperty().getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this availability block");
        }
        availabilityRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public OwnerDashboardResponse dashboard(String email) {
        List<Property> properties = propertyRepository.findAllByOwnerId(owner(email).getId());
        List<Booking> bookings = bookingRepository.findAllByPropertyOwnerIdOrderByCheckInDesc(owner(email).getId());
        double earnings = bookings.stream().filter(b -> "ACCEPTED".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus()))
                .mapToDouble(b -> ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut()) * b.getProperty().getPricePerNight()).sum();
        return new OwnerDashboardResponse(properties.size(), properties.stream().filter(p -> "APPROVED".equals(p.getApprovalStatus())).count(),
                properties.stream().filter(p -> "PENDING".equals(p.getApprovalStatus())).count(),
                bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count(),
                bookings.stream().filter(b -> "ACCEPTED".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus())).count(),
                bookings.stream().filter(b -> b.getCheckOut().isBefore(LocalDate.now()) && ("ACCEPTED".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus()))).count(), earnings);
    }

    private User owner(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner not found")); }
    private Property ownedProperty(Long id, String email) { Property property = propertyRepository.findById(id).orElseThrow(() -> notFound("Property")); if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property"); return property; }
    private PropertyResponse toProperty(Property p) { return new PropertyResponse(p.getId(), p.getTitle(), p.getDescription(), p.getLocation(), p.getPricePerNight(), p.getRating(), p.getApprovalStatus(), p.getOwner().getEmail(), imageRepository.findByPropertyId(p.getId()).stream().map(i -> "/images/" + i.getRelativePath()).toList()); }
    private BookingResponse toBooking(Booking b) { Property p = b.getProperty(); return new BookingResponse(b.getId(), p.getId(), p.getTitle(), p.getLocation(), b.getCheckIn(), b.getCheckOut(), b.getStatus()); }
    private AvailabilityBlockResponse toAvailability(AvailabilityBlock b) { return new AvailabilityBlockResponse(b.getId(), b.getProperty().getId(), b.getStartDate(), b.getEndDate(), b.getReason()); }
    private ResponseStatusException notFound(String resource) { return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found"); }
}

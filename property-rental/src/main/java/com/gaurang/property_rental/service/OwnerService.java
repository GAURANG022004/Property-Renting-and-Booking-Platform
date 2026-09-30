package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.dto.owner.*;
import com.gaurang.property_rental.model.AvailabilityBlock;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.PropertyAvailabilityWindow;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.AvailabilityBlockRepository;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyImageRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.PropertyAvailabilityWindowRepository;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OwnerService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository imageRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityBlockRepository availabilityRepository;
    private final PropertyAvailabilityWindowRepository availabilityWindowRepository;

    public OwnerService(UserRepository userRepository, PropertyRepository propertyRepository,
                        PropertyImageRepository imageRepository, BookingRepository bookingRepository,
                        AvailabilityBlockRepository availabilityRepository,
                        PropertyAvailabilityWindowRepository availabilityWindowRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.imageRepository = imageRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityWindowRepository = availabilityWindowRepository;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> properties(String email) {
        return propertyRepository.findAllByOwnerId(owner(email).getId()).stream().map(this::toProperty).toList();
    }

    @Transactional
    public PropertyResponse updateProperty(Long propertyId, OwnerPropertyUpdateRequest request, String email) {
        Property property = propertyRepository.findByIdForUpdate(propertyId).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property");
        property.update(request.title().trim(), request.description().trim(), request.location().trim(), request.pricePerNight(), request.mapUrl() == null || request.mapUrl().isBlank() ? null : request.mapUrl().trim());
        property.updateBookingRules(request.maxGuests(), request.checkInTime(), request.checkOutTime(), request.houseRules(),
            request.cancellationFreeHours(), request.refundPercentBeforeDeadline(),
            request.refundPercentWithinDeadline(), request.refundPercentAfterCheckIn());
        return toProperty(property);
    }

    @Transactional
    public void deactivateProperty(Long propertyId, String email) {
        Property property = propertyRepository.findByIdForUpdate(propertyId).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property");
        property.deactivate();
    }

    @Transactional
    public PropertyResponse activateProperty(Long propertyId, String email) {
        Property property = ownedProperty(propertyId, email);
        if (!"INACTIVE".equals(property.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only inactive listings can be reactivated");
        }
        property.reactivate();
        return toProperty(property);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> bookings(String email) {
        return bookingRepository.findAllByPropertyOwnerIdOrderByCheckInDesc(owner(email).getId()).stream().map(this::toBooking).toList();
    }

    @Transactional
    public BookingResponse decideBooking(Long bookingId, BookingDecisionRequest request, String email) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> notFound("Booking"));
        Property property = propertyRepository.findByIdForUpdate(booking.getProperty().getId()).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property's booking");
        }
        if (!"PENDING".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending bookings can be accepted or rejected");
        }
        if ("ACCEPTED".equals(request.status())) {
            if (availabilityRepository.existsOverlappingBlock(property.getId(), booking.getCheckIn(), booking.getCheckOut())
                    || bookingRepository.existsConflictingBooking(property.getId(), booking.getCheckIn(), booking.getCheckOut(), booking.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "These dates are no longer available");
            }
            booking.accept();
        }
        else booking.reject();
        return toBooking(booking);
    }

    @Transactional
    public BookingResponse checkIn(Long bookingId, String email) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> notFound("Booking"));
        if (!booking.getProperty().getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property's booking");
        }
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only paid, confirmed bookings can check in");
        }
        LocalDateTime checkInTime = booking.getCheckIn().atTime(LocalTime.parse(booking.getProperty().getCheckInTime()));
        if (LocalDateTime.now().isBefore(checkInTime)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check-in is not available before " + booking.getProperty().getCheckInTime());
        }
        booking.checkIn();
        return toBooking(booking);
    }

    @Transactional
    public BookingResponse completeCheckout(Long bookingId, String email) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> notFound("Booking"));
        if (!booking.getProperty().getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property's booking");
        }
        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active stays can be checked out");
        }
        booking.complete();
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
        Property property = propertyRepository.findByIdForUpdate(request.propertyId()).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property");
        if (bookingRepository.existsOverlappingBooking(property.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot block dates reserved by a booking");
        }
        AvailabilityBlock block = availabilityRepository.save(new AvailabilityBlock(property, request.startDate(), request.endDate(), request.reason()));
        return toAvailability(block);
    }

    @Transactional
    public void removeAvailabilityBlock(Long id, String email) {
        AvailabilityBlock block = availabilityRepository.findById(id).orElseThrow(() -> notFound("Availability block"));
        Property property = propertyRepository.findByIdForUpdate(block.getProperty().getId()).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this availability block");
        }
        availabilityRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityWindowResponse> availabilityWindows(String email) {
        return availabilityWindowRepository.findAllByPropertyOwnerIdOrderByStartDateAsc(owner(email).getId())
                .stream().map(this::toAvailabilityWindow).toList();
    }

    @Transactional
    public AvailabilityWindowResponse addAvailabilityWindow(AvailabilityWindowRequest request, String email) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be after startDate");
        }
        Property property = propertyRepository.findByIdForUpdate(request.propertyId()).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property");
        if (bookingRepository.existsOverlappingBooking(property.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot mark dates available while they contain a reserved booking");
        }
        if (availabilityRepository.existsOverlappingBlock(property.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Available periods cannot overlap blocked dates");
        }
        if (availabilityWindowRepository.existsOverlappingWindow(property.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This availability range overlaps another range for this property");
        }
        return toAvailabilityWindow(availabilityWindowRepository.save(
                new PropertyAvailabilityWindow(property, request.startDate(), request.endDate())));
    }

    @Transactional
    public void removeAvailabilityWindow(Long id, String email) {
        PropertyAvailabilityWindow window = availabilityWindowRepository.findById(id)
                .orElseThrow(() -> notFound("Availability window"));
        Property property = propertyRepository.findByIdForUpdate(window.getProperty().getId()).orElseThrow(() -> notFound("Property"));
        if (!property.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this availability window");
        }
        availabilityWindowRepository.delete(window);
    }

    @Transactional(readOnly = true)
    public OwnerDashboardResponse dashboard(String email) {
        List<Property> properties = propertyRepository.findAllByOwnerId(owner(email).getId());
        List<Booking> bookings = bookingRepository.findAllByPropertyOwnerIdOrderByCheckInDesc(owner(email).getId());
        double earnings = bookings.stream().filter(b -> "ACTIVE".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
            .mapToDouble(b -> b.getTotalAmount().doubleValue()).sum();
        return new OwnerDashboardResponse(properties.size(), properties.stream().filter(p -> "APPROVED".equals(p.getApprovalStatus())).count(),
                properties.stream().filter(p -> "PENDING".equals(p.getApprovalStatus())).count(),
                bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count(),
                bookings.stream().filter(b -> "PAYMENT_PENDING".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus()) || "ACTIVE".equals(b.getStatus())).count(),
                bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count(), earnings);
    }

    private User owner(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner not found")); }
    private Property ownedProperty(Long id, String email) { Property property = propertyRepository.findById(id).orElseThrow(() -> notFound("Property")); if (!property.getOwner().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this property"); return property; }
    private PropertyResponse toProperty(Property p) { return new PropertyResponse(p.getId(), p.getTitle(), p.getDescription(), p.getLocation(), p.getPricePerNight(), p.getRating(), p.getApprovalStatus(), p.getOwner().getEmail(), imageRepository.findByPropertyId(p.getId()).stream().map(i -> "/images/" + i.getRelativePath()).toList(), p.getMapUrl(), p.getMaxGuests(), p.getCheckInTime(), p.getCheckOutTime(), p.getHouseRules(), p.getCancellationFreeHours(), p.getRefundPercentBeforeDeadline(), p.getRefundPercentWithinDeadline(), p.getRefundPercentAfterCheckIn()); }
    private BookingResponse toBooking(Booking b) { Property p = b.getProperty(); return new BookingResponse(b.getId(), p.getId(), p.getTitle(), p.getLocation(), b.getCheckIn(), b.getCheckOut(), b.getStatus(), b.getTotalAmount().doubleValue(), b.getGuestCount(), b.getBookingType(), p.getMaxGuests(), p.getCheckInTime(), p.getCheckOutTime(), p.getHouseRules(), p.getCancellationFreeHours(), p.getRefundPercentBeforeDeadline(), p.getRefundPercentWithinDeadline(), p.getRefundPercentAfterCheckIn(), b.getRefundAmount(), b.getRefundStatus()); }
    private AvailabilityBlockResponse toAvailability(AvailabilityBlock b) { return new AvailabilityBlockResponse(b.getId(), b.getProperty().getId(), b.getStartDate(), b.getEndDate(), b.getReason()); }
    private AvailabilityWindowResponse toAvailabilityWindow(PropertyAvailabilityWindow w) { return new AvailabilityWindowResponse(w.getId(), w.getProperty().getId(), w.getProperty().getTitle(), w.getStartDate(), w.getEndDate()); }
    private ResponseStatusException notFound(String resource) { return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found"); }
}

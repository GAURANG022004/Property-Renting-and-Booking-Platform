package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.BookingCreateRequest;
import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.AvailabilityCheckRequest;
import com.gaurang.property_rental.dto.AvailabilityCheckResponse;
import com.gaurang.property_rental.dto.CancellationQuoteResponse;
import com.gaurang.property_rental.dto.CancellationResponse;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.model.Payment;
import com.gaurang.property_rental.repository.PropertyAvailabilityWindowRepository;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.UserRepository;
import com.gaurang.property_rental.repository.PaymentRepository;
import com.gaurang.property_rental.repository.AvailabilityBlockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final PropertyAvailabilityWindowRepository availabilityWindowRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public BookingService(
            BookingRepository bookingRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            AvailabilityBlockRepository availabilityBlockRepository,
            PropertyAvailabilityWindowRepository availabilityWindowRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService
    ) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.availabilityWindowRepository = availabilityWindowRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public AvailabilityCheckResponse checkAvailability(AvailabilityCheckRequest request) {
        validateDates(request.checkIn(), request.checkOut());
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        int guestCount = request.guestCount() == null ? 1 : request.guestCount();
        return availabilityFor(property, request.checkIn(), request.checkOut(), guestCount, null);
    }

    @Transactional
    public BookingResponse create(Long propertyId, LocalDate checkIn, LocalDate checkOut, Integer requestedGuests, String userEmail) {
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Property property = propertyRepository.findByIdForUpdate(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (!"APPROVED".equals(property.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }

        int guestCount = requestedGuests == null ? 1 : requestedGuests;
        AvailabilityCheckResponse availability = availabilityFor(property, checkIn, checkOut, guestCount, null);
        if (!availability.available()) throw new ResponseStatusException(HttpStatus.CONFLICT, availability.message());

        Booking booking = new Booking(checkIn, checkOut, property, user, guestCount, availability.instantBooking());
        if (booking.getTotalAmount().compareTo(BigDecimal.ONE) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking total must be at least ₹1 for Razorpay payment");
        }
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private AvailabilityCheckResponse availabilityFor(Property property, LocalDate checkIn, LocalDate checkOut,
                                                       int guestCount, Long exceptBookingId) {
        validateDates(checkIn, checkOut);
        if (!"APPROVED".equals(property.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        if (guestCount < 1 || guestCount > property.getMaxGuests()) {
            return new AvailabilityCheckResponse(false, false, BigDecimal.ZERO, guestCount,
                    property.getMaxGuests(), "Guest count exceeds this property's maximum occupancy.");
        }
        if (availabilityBlockRepository.existsOverlappingBlock(property.getId(), checkIn, checkOut)) {
            return new AvailabilityCheckResponse(false, false, BigDecimal.ZERO, guestCount,
                    property.getMaxGuests(), "The selected dates include dates blocked by the owner.");
        }
        boolean conflict = exceptBookingId == null
                ? bookingRepository.existsOverlappingBooking(property.getId(), checkIn, checkOut)
                : bookingRepository.existsConflictingBooking(property.getId(), checkIn, checkOut, exceptBookingId);
        if (conflict) {
            return new AvailabilityCheckResponse(false, false, BigDecimal.ZERO, guestCount,
                    property.getMaxGuests(), "These dates overlap another reserved booking. Choose different dates.");
        }
        List<com.gaurang.property_rental.model.PropertyAvailabilityWindow> windows =
            availabilityWindowRepository.findAllByPropertyIdOrderByStartDateAsc(property.getId());
        boolean instant = windows.stream()
            .anyMatch(window -> !checkIn.isBefore(window.getStartDate()) && !checkOut.isAfter(window.getEndDate()));
        if (!windows.isEmpty() && !instant) {
            return new AvailabilityCheckResponse(false, false, BigDecimal.ZERO, guestCount,
                property.getMaxGuests(), "The selected dates are outside the owner's available periods.");
        }
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = BigDecimal.valueOf(property.getPricePerNight()).multiply(BigDecimal.valueOf(nights))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return new AvailabilityCheckResponse(true, instant, total, guestCount, property.getMaxGuests(),
                instant ? "Available for instant booking." : "Available to request from the owner.");
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in cannot be in the past");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> history(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return bookingRepository.findAllByUserIdOrderByCheckInDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
        public CancellationQuoteResponse cancellationQuote(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view cancellation details for your own bookings");
        }
        return cancellationQuote(booking);
        }

        @Transactional
        public CancellationResponse cancel(Long bookingId, boolean acknowledged, String userEmail) {
        if (!acknowledged) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acknowledge the cancellation policy before cancelling");
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        Property property = propertyRepository.findByIdForUpdate(booking.getProperty().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own bookings");
        }
        CancellationQuoteResponse quote = cancellationQuote(booking);
        if (!quote.eligible()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking can no longer be cancelled");
        String refundStatus = paymentService.processRefund(booking, quote.refundAmount());
        booking.cancel();
        booking.recordRefund(quote.refundAmount(), refundStatus);
        String message = quote.refundAmount().signum() == 0
            ? "Booking cancelled. No refund is due under this property's policy."
            : "Booking cancelled. Refund status: " + refundStatus + ".";
        return new CancellationResponse(booking.getId(), booking.getStatus(), quote.refundAmount(), refundStatus, message);
        }

    @Transactional
    public BookingResponse cancelAsAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        propertyRepository.findByIdForUpdate(booking.getProperty().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        CancellationQuoteResponse quote = cancellationQuote(booking);
        if (!quote.eligible()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking can no longer be cancelled");
        String refundStatus = paymentService.processRefund(booking, quote.refundAmount());
        booking.cancel();
        booking.recordRefund(quote.refundAmount(), refundStatus);
        return toResponse(booking);
    }

        private CancellationQuoteResponse cancellationQuote(Booking booking) {
        String status = booking.getStatus();
        boolean eligible = List.of("PENDING", "PAYMENT_PENDING", "CONFIRMED", "ACTIVE").contains(status);
        Property property = booking.getProperty();
        int refundPercent;
        if ("ACTIVE".equals(status)) {
            refundPercent = property.getRefundPercentAfterCheckIn();
        } else {
            LocalDateTime checkInAt = booking.getCheckIn().atTime(LocalTime.parse(property.getCheckInTime()));
            long hoursUntilCheckIn = Duration.between(LocalDateTime.now(), checkInAt).toHours();
            refundPercent = hoursUntilCheckIn >= property.getCancellationFreeHours()
                ? property.getRefundPercentBeforeDeadline()
                : property.getRefundPercentWithinDeadline();
        }
        BigDecimal amountPaid = paymentRepository.findAllByBookingIdAndStatus(booking.getId(), "COMPLETED").stream()
            .map(payment -> BigDecimal.valueOf(payment.getAmount()).subtract(payment.getRefundAmount()))
            .filter(amount -> amount.signum() > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal refundAmount = amountPaid.multiply(BigDecimal.valueOf(refundPercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String policy = "Cancel at least " + property.getCancellationFreeHours() + " hours before check-in: "
            + property.getRefundPercentBeforeDeadline() + "% refund. Closer to check-in: "
            + property.getRefundPercentWithinDeadline() + "% refund. After check-in: "
            + property.getRefundPercentAfterCheckIn() + "% refund.";
        String refundStatus = amountPaid.signum() == 0 ? "NO_PAYMENT" : refundAmount.signum() == 0 ? "NOT_ELIGIBLE" : "ESTIMATED";
        return new CancellationQuoteResponse(booking.getId(), eligible, policy, refundPercent, amountPaid, refundAmount, refundStatus);
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
                b.getStatus(),
                b.getTotalAmount().doubleValue(),
                b.getGuestCount(),
                b.getBookingType(),
                p.getMaxGuests(), p.getCheckInTime(), p.getCheckOutTime(), p.getHouseRules(),
                p.getCancellationFreeHours(), p.getRefundPercentBeforeDeadline(),
                p.getRefundPercentWithinDeadline(), p.getRefundPercentAfterCheckIn(),
                b.getRefundAmount(), b.getRefundStatus()
        );
    }
}

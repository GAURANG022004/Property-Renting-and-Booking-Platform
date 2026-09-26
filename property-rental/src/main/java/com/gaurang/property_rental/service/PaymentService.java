package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.PaymentCreateRequest;
import com.gaurang.property_rental.dto.PaymentResponse;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Payment;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PaymentRepository;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only pay for your own bookings");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment can only be made for pending bookings");
        }

        // Check if payment already exists for this booking
        if (!paymentRepository.findAllByBookingId(booking.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already processed for this booking");
        }

        // Calculate total amount
        Property property = booking.getProperty();
        long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        double totalAmount = nights * property.getPricePerNight();

        Payment payment = new Payment(
                booking,
                user,
                totalAmount,
                request.paymentMethod(),
                request.transactionId()
        );

        // Simulate payment processing (in real scenario, integrate with payment gateway)
        boolean paymentSuccessful = processPayment(payment);

        if (paymentSuccessful) {
            payment.markAsCompleted();
            booking.decide("CONFIRMED");
        } else {
            payment.markAsFailed("Payment processing failed");
        }

        Payment saved = paymentRepository.save(payment);
        bookingRepository.save(booking);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return paymentRepository.findAllByUserIdOrderByPaymentDateDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetails(Long paymentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own payments");
        }

        return toResponse(payment);
    }

    private boolean processPayment(Payment payment) {
        // Simulate payment processing logic
        // In a real implementation, this would integrate with payment gateways like Stripe, PayPal, etc.
        // For now, we'll simulate success
        return Math.random() > 0.1; // 90% success rate for simulation
    }

    private PaymentResponse toResponse(Payment payment) {
        Booking booking = payment.getBooking();
        Property property = booking.getProperty();

        return new PaymentResponse(
                payment.getId(),
                booking.getId(),
                property.getTitle(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getFailureReason()
        );
    }
}
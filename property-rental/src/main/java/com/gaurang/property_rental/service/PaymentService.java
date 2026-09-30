package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.PaymentResponse;
import com.gaurang.property_rental.dto.RazorpayOrderRequest;
import com.gaurang.property_rental.dto.RazorpayOrderResponse;
import com.gaurang.property_rental.dto.RazorpayVerificationRequest;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Payment;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.BookingRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.PaymentRepository;
import com.gaurang.property_rental.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final com.gaurang.property_rental.repository.AvailabilityBlockRepository availabilityBlockRepository;

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            com.gaurang.property_rental.repository.AvailabilityBlockRepository availabilityBlockRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
    }

    @Transactional
    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request, String userEmail) {
        requireCredentials();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only pay for your own bookings");
        }
        propertyRepository.findByIdForUpdate(booking.getProperty().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        ensureDatesStillAvailable(booking);
        BigDecimal amount = amountForBooking(booking);
        long amountInPaise = amount.movePointRight(2).longValueExact();
        if (amountInPaise < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay requires each payment to be at least ₹1");
        }

        Payment pendingPayment = paymentRepository.findFirstByBookingIdAndStatusOrderByIdDesc(booking.getId(), "PENDING");
        if (pendingPayment != null && BigDecimal.valueOf(pendingPayment.getAmount()).compareTo(amount) == 0) {
            return orderResponse(booking, pendingPayment.getRazorpayOrderId(), amountInPaise);
        }
        if (pendingPayment != null) {
            pendingPayment.markAsFailed("Superseded by the full booking payment flow");
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking-" + booking.getId());
            orderRequest.put("notes", new JSONObject()
                    .put("booking_id", booking.getId()));

            Order razorpayOrder = new RazorpayClient(keyId, keySecret).orders.create(orderRequest);
            String orderId = razorpayOrder.get("id");
            Payment payment = new Payment(booking, user, amount.doubleValue(), "RAZORPAY", orderId);
            paymentRepository.save(payment);
            return orderResponse(booking, orderId, amountInPaise);
        } catch (RazorpayException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to create a Razorpay order", exception);
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(RazorpayVerificationRequest request, String userEmail) {
        requireCredentials();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId());
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Razorpay order not found");
        }
        Booking booking = payment.getBooking();
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only verify your own payments");
        }
        if (!booking.getId().equals(request.bookingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment order does not match this booking");
        }
        if (!verifySignature(request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay payment signature is invalid");
        }
        if ("COMPLETED".equals(payment.getStatus())) {
            if (request.razorpayPaymentId().equals(payment.getTransactionId())) return toResponse(payment);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order has already been paid");
        }
        if (!"PENDING".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment order is no longer pending");
        }

        propertyRepository.findByIdForUpdate(booking.getProperty().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        boolean cancelled = "CANCELLED".equals(booking.getStatus());
        if (!cancelled) ensureDatesStillAvailable(booking);
        BigDecimal expectedAmount = cancelled ? BigDecimal.valueOf(payment.getAmount()) : amountForBooking(booking);
        long expectedPaise = expectedAmount.movePointRight(2).longValueExact();
        try {
            com.razorpay.Payment razorpayPayment = new RazorpayClient(keyId, keySecret).payments.fetch(request.razorpayPaymentId());
            String providerOrderId = razorpayPayment.get("order_id");
            Object providerAmount = razorpayPayment.get("amount");
            String providerStatus = razorpayPayment.get("status");
            if (!request.razorpayOrderId().equals(providerOrderId)
                    || !(providerAmount instanceof Number amountNumber)
                    || amountNumber.longValue() != expectedPaise
                    || !"captured".equals(providerStatus)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay payment is not captured for the expected amount");
            }
        } catch (RazorpayException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to verify the Razorpay payment", exception);
        }

        payment.markAsCompleted(request.razorpayPaymentId());
        if (cancelled) {
            paymentRepository.save(payment);
            BigDecimal refundAmount = expectedAmount.multiply(BigDecimal.valueOf(cancellationRefundPercent(booking)))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            String refundStatus = processRefund(booking, refundAmount);
            booking.recordRefund(refundAmount, refundStatus);
            bookingRepository.save(booking);
            return toResponse(payment);
        }
        booking.confirmPayment();
        bookingRepository.save(booking);
        return toResponse(payment);
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

    @Transactional
    public String processRefund(Booking booking, BigDecimal requestedAmount) {
        if (requestedAmount.signum() <= 0) return "NOT_ELIGIBLE";
        requireCredentials();
        BigDecimal remaining = requestedAmount;
        List<Payment> completedPayments = paymentRepository.findAllByBookingIdAndStatus(booking.getId(), "COMPLETED");
        if (completedPayments.isEmpty()) return "NO_PAYMENT";

        RazorpayClient client;
        try {
            client = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException exception) {
            return "FAILED";
        }
        for (Payment payment : completedPayments) {
            if (remaining.signum() <= 0) break;
            BigDecimal refundable = BigDecimal.valueOf(payment.getAmount()).subtract(payment.getRefundAmount());
            BigDecimal refundAmount = remaining.min(refundable).setScale(2, RoundingMode.HALF_UP);
            if (refundAmount.signum() <= 0) continue;
            try {
                JSONObject request = new JSONObject().put("amount", refundAmount.movePointRight(2).longValueExact());
                Refund refund = client.payments.refund(payment.getTransactionId(), request);
                payment.recordRefund(refundAmount, refund.get("id"), refund.get("status"));
                remaining = remaining.subtract(refundAmount);
            } catch (RazorpayException exception) {
                payment.recordRefundFailure("Razorpay refund request failed");
            }
        }
        return remaining.signum() <= 0 ? "PROCESSING" : "FAILED";
    }

    private BigDecimal amountForBooking(Booking booking) {
        if (!"PAYMENT_PENDING".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is available only after the owner accepts the booking");
        }
        return booking.getTotalAmount();
    }

    private void ensureDatesStillAvailable(Booking booking) {
        if (availabilityBlockRepository.existsOverlappingBlock(booking.getProperty().getId(), booking.getCheckIn(), booking.getCheckOut())
                || bookingRepository.existsConflictingBooking(booking.getProperty().getId(), booking.getCheckIn(), booking.getCheckOut(), booking.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "These dates are no longer available. Contact support before retrying payment.");
        }
    }

    private int cancellationRefundPercent(Booking booking) {
        Property property = booking.getProperty();
        if (booking.isCancelledAfterCheckIn()) return property.getRefundPercentAfterCheckIn();
        LocalDateTime checkInAt = booking.getCheckIn().atTime(LocalTime.parse(property.getCheckInTime()));
        long hoursUntilCheckIn = Duration.between(LocalDateTime.now(), checkInAt).toHours();
        return hoursUntilCheckIn >= property.getCancellationFreeHours()
                ? property.getRefundPercentBeforeDeadline()
                : property.getRefundPercentWithinDeadline();
    }

    private RazorpayOrderResponse orderResponse(Booking booking, String orderId, long amountInPaise) {
        return new RazorpayOrderResponse(booking.getId(), orderId, keyId, amountInPaise, "INR", booking.getProperty().getTitle());
    }

    private void requireCredentials() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = hmac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            byte[] supplied = HexFormat.of().parseHex(signature);
            return MessageDigest.isEqual(expected, supplied);
        } catch (Exception exception) {
            return false;
        }
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
                payment.getFailureReason(),
                payment.getRazorpayOrderId(),
                payment.getRefundAmount(),
                payment.getRefundStatus()
        );
    }
}
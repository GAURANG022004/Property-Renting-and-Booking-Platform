package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.PaymentResponse;
import com.gaurang.property_rental.dto.RazorpayOrderRequest;
import com.gaurang.property_rental.dto.RazorpayOrderResponse;
import com.gaurang.property_rental.dto.RazorpayVerificationRequest;
import com.gaurang.property_rental.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    public RazorpayOrderResponse createOrder(@Valid @RequestBody RazorpayOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return paymentService.createOrder(request, userEmail);
    }

    @PostMapping("/verify")
    public PaymentResponse verifyPayment(@Valid @RequestBody RazorpayVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return paymentService.verifyPayment(request, userEmail);
    }

    @GetMapping
    public List<PaymentResponse> getPaymentHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return paymentService.getPaymentHistory(userEmail);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentDetails(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return paymentService.getPaymentDetails(id, userEmail);
    }
}
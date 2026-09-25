package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.PaymentCreateRequest;
import com.gaurang.property_rental.dto.PaymentResponse;
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

    @PostMapping
    public PaymentResponse createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        return paymentService.createPayment(request, userEmail);
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
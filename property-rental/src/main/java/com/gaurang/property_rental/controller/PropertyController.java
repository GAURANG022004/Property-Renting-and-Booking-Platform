package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.PropertyCreateRequest;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<PropertyResponse> list(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating
    ) {
        return propertyService.listProperties(location, minPrice, maxPrice, minRating);
    }

    @GetMapping("/{id}")
    public PropertyResponse getOne(@PathVariable Long id) {
        return propertyService.getById(id);
    }

    @PostMapping
    public PropertyResponse addProperty(@Valid @RequestBody PropertyCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = (String) auth.getPrincipal();
        return propertyService.createProperty(request, ownerEmail);
    }
}


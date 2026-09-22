package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.PropertyCreateRequest;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.model.PropertyImage;
import com.gaurang.property_rental.repository.PropertyRepository;
import com.gaurang.property_rental.repository.PropertyImageRepository;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyImageRepository propertyImageRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            PropertyImageRepository propertyImageRepository
    ) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> listProperties(String location, Double minPrice, Double maxPrice, Double minRating) {
        List<Property> base;
        if (location == null || location.isBlank()) {
            base = propertyRepository.findAll();
        } else {
            base = propertyRepository.findByLocationContainingIgnoreCase(location);
        }

        return base.stream()
                .filter(p -> "APPROVED".equals(p.getApprovalStatus()))
                .filter(p -> minPrice == null || p.getPricePerNight() >= minPrice)
                .filter(p -> maxPrice == null || p.getPricePerNight() <= maxPrice)
                .filter(p -> minRating == null || p.getRating() >= minRating)
                .sorted(Comparator.comparingDouble(Property::getRating).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse getById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!"APPROVED".equals(property.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        return toResponse(property);
    }

    @Transactional
    public PropertyResponse createProperty(PropertyCreateRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner not found"));

        Property property = new Property(
                request.title(),
                request.description(),
                request.location(),
                request.pricePerNight(),
                request.rating(),
                owner
        );

        Property saved = propertyRepository.save(property);
        return toResponse(saved);
    }

    private PropertyResponse toResponse(Property p) {
        List<String> imageUrls = propertyImageRepository.findByPropertyId(p.getId()).stream()
                .map(img -> "/images/" + img.getRelativePath())
                .toList();

        return new PropertyResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getLocation(),
                p.getPricePerNight(),
                p.getRating(),
                p.getApprovalStatus(),
                p.getOwner() != null ? p.getOwner().getEmail() : null,
                imageUrls
        );
    }
}

package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.service.ImageStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyImageController {

    private final ImageStorageService imageStorageService;

    public PropertyImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/{propertyId}/images")
    public List<String> uploadImages(
            @PathVariable Long propertyId,
            @RequestParam("images") List<MultipartFile> images
    ) {
        Authentication auth = (Authentication) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        String uploaderEmail = (String) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return imageStorageService.uploadPropertyImages(propertyId, images, uploaderEmail, isAdmin);
    }
}


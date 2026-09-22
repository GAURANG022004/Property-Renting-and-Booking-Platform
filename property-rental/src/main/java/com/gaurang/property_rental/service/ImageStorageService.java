package com.gaurang.property_rental.service;

import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.PropertyImage;
import com.gaurang.property_rental.repository.PropertyImageRepository;
import com.gaurang.property_rental.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;

    private final Path uploadDir;

    public ImageStorageService(
            PropertyRepository propertyRepository,
            PropertyImageRepository propertyImageRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.uploadDir = Paths.get(uploadDir);
    }

    @Transactional
    public List<String> uploadPropertyImages(
            Long propertyId,
            List<MultipartFile> images,
            String uploaderEmail,
            boolean isAdmin
    ) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        boolean isOwner = property.getOwner() != null
                && property.getOwner().getEmail() != null
                && property.getOwner().getEmail().equalsIgnoreCase(uploaderEmail);

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the property owner (or admin) can upload images");
        }

        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No images provided");
        }

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;

            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
            String extension = "";
            int dotIndex = original.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = original.substring(dotIndex).toLowerCase(Locale.ROOT);
            }

            // Basic file-name safety: keep only extension and a UUID prefix.
            String storedFileName = UUID.randomUUID() + extension;
            String relativePath = "properties/" + propertyId + "/" + storedFileName;

            Path target = uploadDir.resolve(relativePath);
            try {
                Files.createDirectories(target.getParent());
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
            }

            PropertyImage propertyImage = new PropertyImage(relativePath, property);
            propertyImageRepository.save(propertyImage);

            urls.add("/images/" + relativePath);
        }

        return urls;
    }
}


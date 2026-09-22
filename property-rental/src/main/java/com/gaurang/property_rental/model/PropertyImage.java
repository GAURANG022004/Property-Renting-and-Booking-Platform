package com.gaurang.property_rental.model;

import jakarta.persistence.*;

@Entity
@Table(name = "property_images")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relative path under the configured uploads dir.
     * Example: properties/12/uuid_filename.jpg
     */
    @Column(nullable = false, length = 500)
    private String relativePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    protected PropertyImage() {
    }

    public PropertyImage(String relativePath, Property property) {
        this.relativePath = relativePath;
        this.property = property;
    }

    public Long getId() {
        return id;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Property getProperty() {
        return property;
    }
}


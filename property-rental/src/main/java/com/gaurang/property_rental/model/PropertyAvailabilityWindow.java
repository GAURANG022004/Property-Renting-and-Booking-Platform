package com.gaurang.property_rental.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "property_availability_windows")
public class PropertyAvailabilityWindow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    protected PropertyAvailabilityWindow() { }

    public PropertyAvailabilityWindow(Property property, LocalDate startDate, LocalDate endDate) {
        this.property = property;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public Property getProperty() { return property; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
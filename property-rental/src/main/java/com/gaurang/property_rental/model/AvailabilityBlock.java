package com.gaurang.property_rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "availability_blocks")
public class AvailabilityBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(length = 300) private String reason;

    protected AvailabilityBlock() { }
    public AvailabilityBlock(Property property, LocalDate startDate, LocalDate endDate, String reason) { this.property = property; this.startDate = startDate; this.endDate = endDate; this.reason = reason; }
    public Long getId() { return id; }
    public Property getProperty() { return property; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
}

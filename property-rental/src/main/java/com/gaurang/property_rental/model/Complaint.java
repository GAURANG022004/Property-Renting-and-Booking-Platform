package com.gaurang.property_rental.model;

import jakarta.persistence.*;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(length = 3000)
    private String resolutionNote;

    protected Complaint() { }

    public Complaint(User reportedBy, String subject, String description) {
        this.reportedBy = reportedBy;
        this.subject = subject;
        this.description = description;
    }

    public Long getId() { return id; }
    public User getReportedBy() { return reportedBy; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getResolutionNote() { return resolutionNote; }
    public void resolve(String status, String resolutionNote) { this.status = status; this.resolutionNote = resolutionNote; }
}

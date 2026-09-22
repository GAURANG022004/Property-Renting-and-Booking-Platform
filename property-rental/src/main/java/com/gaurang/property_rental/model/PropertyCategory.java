package com.gaurang.property_rental.model;

import jakarta.persistence.*;

@Entity
@Table(name = "property_categories")
public class PropertyCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    protected PropertyCategory() { }

    public PropertyCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void update(String name, String description) { this.name = name; this.description = description; }
}

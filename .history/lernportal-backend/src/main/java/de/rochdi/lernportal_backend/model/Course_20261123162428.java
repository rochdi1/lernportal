package de.rochdi.lernportal_backend.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    // Getter & Setter (oder Lombok @Getter/@Setter)
}

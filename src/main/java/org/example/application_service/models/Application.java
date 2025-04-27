package org.example.application_service.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    private LocalDateTime submissionDate;

    // Personal details fields directly
    @Column(nullable = false)
    private String matricule;

    private String firstName;
    private String lastName;
    private String nationality;
    private String regionOfOrigin;
    private String address;
    private String whatsappNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Program program;

    // Other linked entities
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private EducationDetails educationDetails;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private FamilyDetails familyDetails;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;
}

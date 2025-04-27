package org.example.application_service.DTO;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.application_service.models.Document;
import org.example.application_service.models.EducationDetails;
import org.example.application_service.models.FamilyDetails;
import org.example.application_service.models.Program;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.util.List;

@Data
public class ApplicationRequest {
    @NotBlank(message = "Matricule is required")
    private String matricule;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Region of origin is required")
    private String regionOfOrigin;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String whatsappNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Program is required")
    private Program program;

    @Valid
    @NotNull(message = "Family details are required")
    private FamilyDetails familyDetails;

    @Valid
    @NotNull(message = "Education details are required")
    private EducationDetails educationDetails;

    private List<MultipartFile> documents;
}
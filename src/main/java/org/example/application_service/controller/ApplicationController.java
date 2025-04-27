package org.example.application_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.application_service.DTO.ApplicationRequest;
import org.example.application_service.models.*;
import org.example.application_service.services.ApplicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    public ApplicationController(ApplicationService applicationService, ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    // Complete submission with file uploads
    @PostMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitCompleteApplication(
            @RequestPart("application") @Valid String applicationJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

        try {
            ApplicationRequest request = objectMapper.readValue(applicationJson, ApplicationRequest.class);
            Application savedApplication = applicationService.saveCompleteApplication(request, documents);
            return new ResponseEntity<>(savedApplication, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing request: " + e.getMessage());
        }
    }

    // Step 1: Initialize Application
    @PostMapping("/init")
    public ResponseEntity<?> initializeApplication(@RequestBody Application application) {
        try {
            Application savedApplication = applicationService.initializeApplication(application);
            return ResponseEntity.ok(savedApplication);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Step 3: Save Education Details
    @PutMapping("/{applicationId}/education-details")
    public ResponseEntity<Application> updateEducationDetails(@PathVariable Long applicationId, @RequestBody EducationDetails details) {
        Application application = applicationService.updateEducationDetails(applicationId, details);
        return ResponseEntity.ok(application);
    }

    // Step 4: Save Family Details
    @PutMapping("/{applicationId}/family-details")
    public ResponseEntity<Application> updateFamilyDetails(@PathVariable Long applicationId, @RequestBody FamilyDetails details) {
        Application application = applicationService.updateFamilyDetails(applicationId, details);
        return ResponseEntity.ok(application);
    }

    // Step 5: Submit Application
    @PutMapping("/{applicationId}/submit")
    public ResponseEntity<?> submitApplication(@PathVariable Long applicationId) {
        try {
            Application application = applicationService.submitApplication(applicationId);
            return ResponseEntity.ok(application);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{applicationId}/status/{status}")
    public ResponseEntity<Application> updateStatus(@PathVariable Long applicationId, @PathVariable ApplicationStatus status) {
        Application application = applicationService.updateApplicationStatus(applicationId, status);
        return ResponseEntity.ok(application);
    }
    // Step 7: Upload Document
    @PostMapping("/{applicationId}/documents")
    public Application uploadDocuments(
            @PathVariable Long applicationId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("documentTypes") String[] documentTypes) {

        // Ensure that the number of files and document types are the same
        if (files.length != documentTypes.length) {
            throw new RuntimeException("Number of files must match number of document types");
        }

        // Call the service method to add the documents
        return applicationService.addDocuments(applicationId, files, documentTypes);
    }
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Application> getApplicationByMatricule(@PathVariable String matricule) {
        Application application = applicationService.getApplicationByMatricule(matricule);
        return ResponseEntity.ok(application);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }
    @GetMapping("/{applicationId}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long applicationId) {
        Application application = applicationService.getApplicationById(applicationId);
        if (application != null) {
            return ResponseEntity.ok(application);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{applicationId}/documents/{documentId}/preview")
    public ResponseEntity<byte[]> previewDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId) {
        Document document = applicationService.getDocumentById(applicationId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + document.getDocumentType())
                .contentType(MediaType.APPLICATION_PDF) // Adjust based on document type
                .body(document.getDocumentContent());
    }

}

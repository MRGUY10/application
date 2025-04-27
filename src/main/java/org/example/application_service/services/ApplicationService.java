package org.example.application_service.services;

import jakarta.transaction.Transactional;
import org.example.application_service.DTO.ApplicationRequest;
import org.example.application_service.Request.MatriculeUpdateRequest;
import org.example.application_service.models.*;
import org.example.application_service.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    private String authServiceUrl = "http://20.11.21.61:8087";

    @Autowired
    private EducationDetailsRepository educationDetailsRepository;

    @Autowired
    private FamilyDetailsRepository familyDetailsRepository;

    @Autowired
    private DocumentRepository documentRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              DocumentRepository documentRepository) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public Application saveCompleteApplication(ApplicationRequest request, List<MultipartFile> documentFiles) {
        // Create and save the main application
        Application application = new Application();
        application.setMatricule(request.getMatricule());
        application.setFirstName(request.getFirstName());
        application.setLastName(request.getLastName());
        application.setNationality(request.getNationality());
        application.setRegionOfOrigin(request.getRegionOfOrigin());
        application.setAddress(request.getAddress());
        application.setWhatsappNumber(request.getWhatsappNumber());
        application.setEmail(request.getEmail());
        application.setDateOfBirth(request.getDateOfBirth());
        application.setPhoneNumber(request.getPhoneNumber());
        application.setProgram(request.getProgram());
        application.setStatus(ApplicationStatus.SUBMITTED);

        // Set and save family details
        FamilyDetails familyDetails = request.getFamilyDetails();
        familyDetails.setApplication(application);
        application.setFamilyDetails(familyDetails);

        // Set and save education details
        EducationDetails educationDetails = request.getEducationDetails();
        educationDetails.setApplication(application);
        application.setEducationDetails(educationDetails);

        // Save the application (cascade will save family and education details)
        Application savedApplication = applicationRepository.save(application);

        // Process and save documents if provided
        if (documentFiles != null && !documentFiles.isEmpty()) {
            List<Document> documents = new ArrayList<>();
            for (MultipartFile file : documentFiles) {
                try {
                    Document document = new Document();
                    document.setDocumentType(file.getOriginalFilename().contains("id") ? "ID_CARD" : "OTHER");
                    document.setDocumentContent(file.getBytes());
                    document.setApplication(savedApplication);
                    documents.add(document);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process document: " + file.getOriginalFilename(), e);
                }
            }
            documentRepository.saveAll(documents);
            savedApplication.setDocuments(documents);
        }

        return savedApplication;
    }


    // Step 1: Initialize application
    public Application initializeApplication(Application application) {
        if (application.getProgram() == null) {
            throw new IllegalArgumentException("Program selection is required");
        }

        Optional<Application> existingApplication = applicationRepository.findByMatriculeAndProgram(application.getMatricule(), application.getProgram());
        if (existingApplication.isPresent()) {
            throw new IllegalStateException("You have already applied for the " + application.getProgram() + " program.");
        }

        return applicationRepository.save(application);
    }

    // Step 2: Save Education Details
    public Application updateEducationDetails(Long applicationId, EducationDetails details) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        details.setApplication(application);
        educationDetailsRepository.save(details);
        application.setEducationDetails(details);

        return applicationRepository.save(application);
    }

    // Step 3: Save Family Details
    public Application updateFamilyDetails(Long applicationId, FamilyDetails details) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        details.setApplication(application);
        familyDetailsRepository.save(details);
        application.setFamilyDetails(details);

        return applicationRepository.save(application);
    }

    // Step 4: Submit Application
    public Application submitApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getFirstName() == null ||
                application.getLastName() == null ||
                application.getEmail() == null ||
                application.getPhoneNumber() == null ||
                application.getEducationDetails() == null ||
                application.getFamilyDetails() == null) {
            throw new IllegalStateException("All details must be completed before submitting the application.");
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmissionDate(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    // Step 5: Update Application Status
    public Application updateApplicationStatus(Long applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() == ApplicationStatus.STUDENT) {
            throw new RuntimeException("Status cannot be updated once the candidate is a student");
        }

        application.setStatus(status);

        if (status == ApplicationStatus.STUDENT) {
            String programCode = getProgramCode(String.valueOf(application.getProgram()));
            String uniqueNumber = String.format("%04d", application.getId());
            String year = String.valueOf(java.time.Year.now().getValue());

            String newMatricule = year + programCode + uniqueNumber;

            String oldMatricule = application.getMatricule();
            application.setMatricule(newMatricule);

            if (oldMatricule != null) {
                updateAuthServiceWithMatricule(oldMatricule, newMatricule);
            } else {
                registerNewMatriculeInAuthService(newMatricule);
            }
        }

        applicationRepository.save(application);

        sendStatusEmail(application, status);

        return application;
    }

    private void sendStatusEmail(Application application, ApplicationStatus status) {
        String candidateEmail = application.getEmail();
        String candidateFirstName = application.getFirstName();
        String subject = "Application Status Update";
        String htmlBody = generateHtmlTemplate(candidateFirstName, status);

        emailService.sendEmail(candidateEmail, subject, htmlBody);
    }

    private String generateHtmlTemplate(String candidateName, ApplicationStatus status) {
        switch (status) {
            case SUBMITTED:
                return "Dear " + candidateName + ",\n\n"
                        + "We are pleased to inform you that your application has been successfully submitted and received by our team. Thank you for taking the time to apply.\n\n"
                        + "Our admissions office is currently reviewing your application, and you will be notified once the next steps are available.\n\n"
                        + "If you have any questions, feel free to reach out to us at [Contact Email/Phone].\n\n"
                        + "Best regards,\nAdmissions Office";

            case APPLICATION_REJECTED:
                return "Dear " + candidateName + ",\n\n"
                        + "Thank you for submitting your application to [School Name]. After a thorough review, we regret to inform you that your application was not successful at this time.\n\n"
                        + "We deeply appreciate your interest in joining our institution and encourage you to consider applying again in the future.\n\n"
                        + "Should you have any questions or need further clarification, please do not hesitate to contact us at [Contact Email/Phone].\n\n"
                        + "Wishing you the very best,\nAdmissions Office";

            case APPLICATION_ACCEPTED:
                return "Dear " + candidateName + ",\n\n"
                        + "Congratulations! We are delighted to inform you that your application has been accepted for admission to [School Name].\n\n"
                        + "You can now proceed to register for the entrance examination, which must be completed by [Deadline Date]. Instructions for exam registration and preparation will be provided shortly.\n\n"
                        + "We are excited about your journey with us and look forward to seeing you excel. Please contact us at [Contact Email/Phone] should you require any assistance.\n\n"
                        + "Warm regards,\nAdmissions Office";

            case EXAM_REGISTERED:
                return "Dear " + candidateName + ",\n\n"
                        + "We are pleased to confirm that you have successfully registered for the entrance examination. We recommend preparing thoroughly to showcase your strengths and skills.\n\n"
                        + "Your examination details are as follows:\n"
                        + "- **Date:** [Exam Date]\n"
                        + "- **Location:** [Exam Location]\n\n"
                        + "Should you have any questions, please contact us at [Contact Email/Phone]. Best wishes for your upcoming examination!\n\n"
                        + "Sincerely,\nAdmissions Office";

            case ADMISSION_OFFERED:
                return "Dear " + candidateName + ",\n\n"
                        + "We are thrilled to inform you that you have been offered admission to [School Name]! Your dedication and hard work have truly paid off, and we are excited to welcome you to our community.\n\n"
                        + "To finalize your enrollment, please complete the necessary admission steps by [Deadline Date]. Details for next steps will be provided soon.\n\n"
                        + "Welcome to [School Name]! Should you require assistance, feel free to contact us at [Contact Email/Phone].\n\n"
                        + "Warm regards,\nAdmissions Office";

            case ADMISSION_REJECTED:
                return "Dear " + candidateName + ",\n\n"
                        + "Thank you for applying to [School Name]. After careful consideration, we regret to inform you that your application has not been successful in securing admission.\n\n"
                        + "We value your interest in our institution and encourage you to explore other opportunities or reapply in the future.\n\n"
                        + "If you have any questions or need further assistance, please contact us at [Contact Email/Phone].\n\n"
                        + "Wishing you all the best in your academic journey,\nAdmissions Office";

            default:
                return "Dear " + candidateName + ",\n\n"
                        + "Your application status has been updated.\n\n"
                        + "Best regards,\nAdmissions Office";
        }
    }

    private String getProgramCode(String program) {
        switch (program) {
            case "ENGINEERING":
                return "ENG";
            case "MANAGEMENT":
                return "MGT";
            case "LICENSE":
                return "LCS";
            default:
                return "GEN";
        }
    }

    private void updateAuthServiceWithMatricule(String oldMatricule, String newMatricule) {
        // Construct URL with path variable
        String url = authServiceUrl + "/api/v1/candidate/update-matricule/{oldMatricule}";

        // Using URI template with path variable and request param
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("newMatricule", newMatricule);

        try {
            // Make the PUT request with null body (since we're using query parameters)
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.buildAndExpand(oldMatricule).toUri(),
                    HttpMethod.PUT,
                    null,  // No request body needed
                    String.class
            );

            // Verify successful response
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Auth service returned status: " + response.getStatusCode());
            }

        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to communicate with auth service: " + e.getMessage(), e);
        }
    }



    // Separate method to register a new matricule if no old matricule exists
    private void registerNewMatriculeInAuthService(String newMatricule) {
        String url = authServiceUrl + "/api/v1/candidate/register-matricule";

        MatriculeUpdateRequest request = new MatriculeUpdateRequest(newMatricule);

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request), String.class);
        } catch (HttpServerErrorException e) {
            System.err.println("Error while registering new matricule in auth service: " + e.getResponseBodyAsString());
        }
    }


    // Helper method to get program code (customize as needed)
    private String getProgramCode(Program program) {
        if (program == null) {
            return "OTH"; // Default code for unknown programs
        }

        switch (program) {
            case ENGINEERING:
                return "ING";
            case MANAGEMENT:
                return "MGT";
            case LICENSE:
                return "LIC";
            default:
                return "OTH";
        }
    }


    // Method to upload a document and store it as a BLOB in the database
    public Application addDocuments(Long applicationId, MultipartFile[] files, String[] documentTypes) {
        // Fetch the application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        try {
            // Iterate through each file and its corresponding document type
            for (int i = 0; i < files.length; i++) {
                // Convert the uploaded file to byte array
                byte[] documentContent = files[i].getBytes();

                // Create the document entity and save binary data
                Document document = new Document();
                document.setDocumentType(documentTypes[i]);  // Set the document type (from the array)
                document.setDocumentContent(documentContent);  // Store binary data in the BLOB field
                document.setApplication(application);  // Associate the document with the application

                // Save the document in the database
                documentRepository.save(document);

                // Add the document to the application and update the application
                application.getDocuments().add(document);
            }

            // Save the updated application with the new documents
            applicationRepository.save(application);

            return application;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store the documents", e);
        }
    }
    // Fetch application by matricule
    public Application getApplicationByMatricule(String matricule) {
        Optional<Application> application = applicationRepository.findByMatricule(matricule);
        if (application.isPresent()) {
            return application.get();
        } else {
            throw new RuntimeException("Application not found for matricule: " + matricule);
        }
    }
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }



    public Document getDocumentById(Long applicationId, Long documentId) {
        // Fetch document from database (ensure you have a repository)
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public Application getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

}

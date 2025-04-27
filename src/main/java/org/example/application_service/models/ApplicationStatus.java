package org.example.application_service.models;

public enum ApplicationStatus {
    DRAFT,           // Application is in progress
    SUBMITTED,       // Submitted by candidate
    APPLICATION_REJECTED, // Rejected during initial review
    APPLICATION_ACCEPTED, // Passed initial review
    EXAM_REGISTERED, //passed exam
    ADMISSION_OFFERED,   // Final admission offered
    ADMISSION_REJECTED,  // Final rejection
    STUDENT;
}
package org.example.application_service.Request;

public class MatriculeUpdateRequest {
    private String matricule;

    // Constructor that accepts a matricule string
    public MatriculeUpdateRequest(String matricule) {
        this.matricule = matricule;
    }

    // Getter and Setter for matricule
    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
}

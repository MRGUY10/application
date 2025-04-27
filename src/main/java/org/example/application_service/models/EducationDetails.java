package org.example.application_service.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EducationDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean hasAL;
    private String upperSixthSeries;
    private int totalNumberInClass;
    private String town;
    private String school;
    private Boolean repeatedClass;
    private String alGrades;
    private String olGrades;
    private String termPositions;
    private String chosenWritingCenter;
    private String chosenField;

    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHasAL() {
        return hasAL;
    }

    public void setHasAL(Boolean hasAL) {
        this.hasAL = hasAL;
    }

    public String getUpperSixthSeries() {
        return upperSixthSeries;
    }

    public void setUpperSixthSeries(String upperSixthSeries) {
        this.upperSixthSeries = upperSixthSeries;
    }

    public int getTotalNumberInClass() {
        return totalNumberInClass;
    }

    public void setTotalNumberInClass(int totalNumberInClass) {
        this.totalNumberInClass = totalNumberInClass;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Boolean getRepeatedClass() {
        return repeatedClass;
    }

    public void setRepeatedClass(Boolean repeatedClass) {
        this.repeatedClass = repeatedClass;
    }

    public String getAlGrades() {
        return alGrades;
    }

    public void setAlGrades(String alGrades) {
        this.alGrades = alGrades;
    }

    public String getOlGrades() {
        return olGrades;
    }

    public void setOlGrades(String olGrades) {
        this.olGrades = olGrades;
    }

    public String getTermPositions() {
        return termPositions;
    }

    public void setTermPositions(String termPositions) {
        this.termPositions = termPositions;
    }

    public String getChosenWritingCenter() {
        return chosenWritingCenter;
    }

    public void setChosenWritingCenter(String chosenWritingCenter) {
        this.chosenWritingCenter = chosenWritingCenter;
    }

    public String getChosenField() {
        return chosenField;
    }

    public void setChosenField(String chosenField) {
        this.chosenField = chosenField;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}

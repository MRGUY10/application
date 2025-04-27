package org.example.application_service.repo;

import org.example.application_service.models.Application;
import org.example.application_service.models.ApplicationStatus;
import org.example.application_service.models.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByMatriculeAndProgram(String matricule, Program program);
    Optional<Application> findByMatricule(String matricule);
}

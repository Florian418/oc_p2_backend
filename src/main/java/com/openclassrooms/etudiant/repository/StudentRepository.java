package com.openclassrooms.etudiant.repository;

import com.openclassrooms.etudiant.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour l'accès aux données des étudiants
 * (CRUD fourni par JpaRepository, sans requête personnalisée).
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}

package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Service métier gérant les opérations CRUD sur les étudiants, entre le
 * controller REST et le repository JPA.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Persiste un nouvel étudiant.
     */
    public Student create(Student student) {
        Assert.notNull(student, "Student must not be null");
        log.info("Creating new student");
        return studentRepository.save(student);
    }

    /**
     * Renvoie la liste de tous les étudiants.
     */
    public List<Student> findAll() {
        log.info("Fetching all students");
        return studentRepository.findAll();
    }

    /**
     * Renvoie l'étudiant correspondant à l'id, ou Optional vide s'il n'existe pas.
     */
    public Optional<Student> findById(Long id) {
        log.info("Fetching student with id {}", id);
        return studentRepository.findById(id);
    }

    /**
     * Supprime l'étudiant correspondant à l'id. Renvoie false sans rien
     * supprimer si l'id n'existe pas.
     */
    public boolean delete(Long id) {
        log.info("Deleting student with id {}", id);
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }

    /**
     * Met à jour les champs de l'étudiant existant correspondant à l'id.
     * Renvoie Optional vide si l'id n'existe pas.
     */
    public Optional<Student> update(Long id, Student updated) {
        Assert.notNull(updated, "Student must not be null");
        log.info("Updating student with id {}", id);
        return studentRepository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setEmail(updated.getEmail());
            existing.setStudentNumber(updated.getStudentNumber());
            return studentRepository.save(existing);
        });
    }
}

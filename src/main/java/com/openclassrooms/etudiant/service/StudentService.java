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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student create(Student student) {
        Assert.notNull(student, "Student must not be null");
        log.info("Creating new student");
        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        log.info("Fetching all students");
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        log.info("Fetching student with id {}", id);
        return studentRepository.findById(id);
    }

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

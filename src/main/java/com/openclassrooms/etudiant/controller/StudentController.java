package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.mapper.StudentMapper;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST exposant les opérations CRUD sur les étudiants
 * (/api/students), protégé par le filtre JWT. N'expose que des DTOs,
 * jamais l'entité Student.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentMapper studentMapper;

    /**
     * POST /api/students : crée un étudiant et renvoie 201 avec la ressource créée.
     */
    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(@Valid @RequestBody StudentRequestDTO dto) {
        StudentResponseDTO response = studentMapper.toDto(
                studentService.create(studentMapper.toEntity(dto))
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/students : renvoie la liste de tous les étudiants.
     */
    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> findAll() {
        List<StudentResponseDTO> students = studentService.findAll()
                .stream()
                .map(studentMapper::toDto)
                .toList();
        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/students/{id} : renvoie l'étudiant demandé, ou 404 s'il n'existe pas.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> findById(@PathVariable Long id) {
        return studentService.findById(id)
                .map(studentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/students/{id} : met à jour l'étudiant demandé, ou renvoie 404 s'il n'existe pas.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody StudentRequestDTO dto) {
        return studentService.update(id, studentMapper.toEntity(dto))
                .map(studentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/students/{id} : supprime l'étudiant demandé (204),
     * ou renvoie 404 s'il n'existe pas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return studentService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

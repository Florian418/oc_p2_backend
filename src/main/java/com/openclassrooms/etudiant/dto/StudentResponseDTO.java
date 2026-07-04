package com.openclassrooms.etudiant.dto;

import lombok.Data;

/**
 * DTO de sortie exposant les données d'un étudiant dans les réponses de
 * l'API /api/students (jamais l'entité JPA directement).
 */
@Data
public class StudentResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String studentNumber;
}

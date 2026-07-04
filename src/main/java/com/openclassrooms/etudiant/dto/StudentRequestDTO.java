package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO d'entrée pour la création/mise à jour d'un étudiant via l'API
 * (reçu dans le corps des requêtes POST/PUT /api/students).
 */
@Data
public class StudentRequestDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String email;

    @NotBlank
    private String studentNumber;
}

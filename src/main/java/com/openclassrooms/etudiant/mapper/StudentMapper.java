package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper MapStruct assurant la conversion entre l'entité Student et ses DTOs,
 * afin que les controllers ne manipulent jamais l'entité directement.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StudentMapper {

    /**
     * Convertit un DTO de requête en entité ; id, created_at et updated_at
     * sont ignorés car gérés par la base de données (auto-généré / auto-daté).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    Student toEntity(StudentRequestDTO dto);

    /**
     * Convertit une entité en DTO de réponse exposé par l'API.
     */
    StudentResponseDTO toDto(Student student);
}

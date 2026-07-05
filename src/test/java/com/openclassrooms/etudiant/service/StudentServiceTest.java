package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de StudentService : opérations CRUD sur les étudiants,
 * avec StudentRepository mocké.
 */
@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
    private static final Long ID = 1L;
    private static final Long UNKNOWN_ID = 99L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@test.com";
    private static final String STUDENT_NUMBER = "S123";

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;

    private Student newStudent() {
        Student student = new Student();
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setEmail(EMAIL);
        student.setStudentNumber(STUDENT_NUMBER);
        return student;
    }

    @Test
    public void test_create_student() {
        // GIVEN un étudiant valide
        Student student = newStudent();
        when(studentRepository.save(student)).thenReturn(student);

        // WHEN on le crée
        studentService.create(student);

        // THEN le repository sauvegarde bien cet étudiant
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(student);
    }

    @Test
    public void test_findAll_returns_all_students() {
        // GIVEN plusieurs étudiants en base
        Student student1 = newStudent();
        Student student2 = new Student();
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@test.com");
        student2.setStudentNumber("S456");
        Student student3 = new Student();
        student3.setFirstName("Bob");
        student3.setLastName("Martin");
        student3.setEmail("bob.martin@test.com");
        student3.setStudentNumber("S789");
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2, student3));

        // WHEN on récupère la liste
        List<Student> result = studentService.findAll();

        // THEN la liste renvoyée correspond à celle du repository
        assertThat(result).containsExactly(student1, student2, student3);
    }

    @Test
    public void test_findAll_no_student_returns_empty_list() {
        // GIVEN aucun étudiant en base
        when(studentRepository.findAll()).thenReturn(List.of());

        // WHEN on récupère la liste
        List<Student> result = studentService.findAll();

        // THEN la liste renvoyée est vide
        assertThat(result).isEmpty();
    }

    @Test
    public void test_findById_existing_id_returns_student() {
        // GIVEN un étudiant existant
        Student student = newStudent();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(student));

        // WHEN on le recherche par id
        Optional<Student> result = studentService.findById(ID);

        // THEN l'étudiant est renvoyé
        assertThat(result).contains(student);
    }

    @Test
    public void test_findById_unknown_id_returns_empty() {
        // GIVEN aucun étudiant pour cet id
        when(studentRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        // WHEN on le recherche par id
        Optional<Student> result = studentService.findById(UNKNOWN_ID);

        // THEN aucun résultat n'est renvoyé
        assertThat(result).isEmpty();
    }

    @Test
    public void test_update_existing_id_updates_and_returns_student() {
        // GIVEN un étudiant existant et de nouvelles valeurs
        Student existing = newStudent();
        Student updatedValues = new Student();
        updatedValues.setFirstName("Jane");
        updatedValues.setLastName("Smith");
        updatedValues.setEmail("jane.smith@test.com");
        updatedValues.setStudentNumber("S456");
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any())).thenReturn(existing);

        // WHEN on met à jour cet étudiant
        Optional<Student> result = studentService.update(ID, updatedValues);

        // THEN les champs sont mis à jour et le résultat est renvoyé
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Jane");
        assertThat(result.get().getLastName()).isEqualTo("Smith");
        assertThat(result.get().getEmail()).isEqualTo("jane.smith@test.com");
        assertThat(result.get().getStudentNumber()).isEqualTo("S456");
        verify(studentRepository).save(existing);
    }

    @Test
    public void test_update_unknown_id_returns_empty_and_never_saves() {
        // GIVEN aucun étudiant pour cet id
        when(studentRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        // WHEN on tente de le mettre à jour
        Optional<Student> result = studentService.update(UNKNOWN_ID, newStudent());

        // THEN aucun résultat n'est renvoyé et rien n'est sauvegardé
        assertThat(result).isEmpty();
        verify(studentRepository, never()).save(any());
    }

    @Test
    public void test_delete_existing_id_returns_true() {
        // GIVEN un étudiant existant
        when(studentRepository.existsById(ID)).thenReturn(true);

        // WHEN on le supprime
        boolean result = studentService.delete(ID);

        // THEN la suppression est confirmée et le repository est appelé
        assertThat(result).isTrue();
        verify(studentRepository).deleteById(ID);
    }

    @Test
    public void test_delete_unknown_id_returns_false() {
        // GIVEN aucun étudiant pour cet id
        when(studentRepository.existsById(UNKNOWN_ID)).thenReturn(false);

        // WHEN on tente de le supprimer
        boolean result = studentService.delete(UNKNOWN_ID);

        // THEN la suppression est refusée et jamais appelée sur le repository
        assertThat(result).isFalse();
        verify(studentRepository, never()).deleteById(any());
    }
}

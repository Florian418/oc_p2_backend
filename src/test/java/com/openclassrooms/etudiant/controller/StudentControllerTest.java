package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.JwtService;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Tests d'intégration de StudentController.
 *
 * Contrairement à un test unitaire (dépendances mockées), un test
 * d'intégration démarre un vrai contexte Spring et fait collaborer les
 * vraies classes entre elles (controller → service → repository → base de
 * données), pour vérifier qu'elles s'assemblent correctement — ici via
 * MockMvc (simule la requête HTTP) et un vrai MySQL éphémère (Testcontainers).
 *
 * Les routes /api/students sont protégées par JwtAuthFilter, chaque requête
 * est donc envoyée avec un token JWT valide (obtenu via un vrai utilisateur
 * enregistré en base).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class StudentControllerTest {

    private static final String URL = "/api/students";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@test.com";
    private static final String STUDENT_NUMBER = "S123";
    private static final Long UNKNOWN_ID = 999L;

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.10");

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private String token;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    /**
     * Enregistre un utilisateur réel et génère son token JWT, requis par
     * JwtAuthFilter pour accéder aux routes /api/students.
     */
    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        userService.register(user);
        token = jwtService.generateToken(user);
    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private StudentRequestDTO newStudentRequestDTO() {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setEmail(EMAIL);
        dto.setStudentNumber(STUDENT_NUMBER);
        return dto;
    }

    private Student saveStudent() {
        return saveStudent(EMAIL, STUDENT_NUMBER);
    }

    private Student saveStudent(String email, String studentNumber) {
        Student student = new Student();
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setEmail(email);
        student.setStudentNumber(studentNumber);
        return studentRepository.save(student);
    }

    /**
     * Ajoute le header Authorization (Bearer token) à une requête MockMvc,
     * requis par JwtAuthFilter pour accéder aux routes /api/students.
     */
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + token);
    }

    @Test
    public void createStudentSuccessful() throws Exception {
        // GIVEN un dto valide et un token valide
        StudentRequestDTO dto = newStudentRequestDTO();

        // WHEN on crée l'étudiant
        // THEN il est créé (201) et renvoyé dans le corps de la réponse
        mockMvc.perform(withAuth(MockMvcRequestBuilders.post(URL))
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(EMAIL));
    }

    @Test
    public void findAllStudents() throws Exception {
        // GIVEN plusieurs étudiants existants en base
        saveStudent(EMAIL, STUDENT_NUMBER);
        saveStudent("jane.smith@test.com", "S456");

        // WHEN on récupère la liste
        // THEN elle contient bien les deux étudiants
        mockMvc.perform(withAuth(MockMvcRequestBuilders.get(URL))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    public void findByIdExisting() throws Exception {
        // GIVEN un étudiant existant en base
        Student student = saveStudent();

        // WHEN on le recherche par son id
        // THEN il est renvoyé (200)
        mockMvc.perform(withAuth(MockMvcRequestBuilders.get(URL + "/" + student.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(EMAIL));
    }

    @Test
    public void findByIdUnknown() throws Exception {
        // GIVEN aucun étudiant pour cet id
        // WHEN on le recherche
        // THEN 404
        mockMvc.perform(withAuth(MockMvcRequestBuilders.get(URL + "/" + UNKNOWN_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void updateStudentExisting() throws Exception {
        // GIVEN un étudiant existant et de nouvelles valeurs
        Student student = saveStudent();
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane.smith@test.com");
        dto.setStudentNumber("S456");

        // WHEN on le met à jour
        // THEN il est renvoyé mis à jour (200)
        mockMvc.perform(withAuth(MockMvcRequestBuilders.put(URL + "/" + student.getId()))
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Jane"));
    }

    @Test
    public void updateStudentUnknown() throws Exception {
        // GIVEN aucun étudiant pour cet id
        // WHEN on tente de le mettre à jour
        // THEN 404
        mockMvc.perform(withAuth(MockMvcRequestBuilders.put(URL + "/" + UNKNOWN_ID))
                        .content(objectMapper.writeValueAsString(newStudentRequestDTO()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteStudentExisting() throws Exception {
        // GIVEN un étudiant existant
        Student student = saveStudent();

        // WHEN on le supprime
        // THEN 204
        mockMvc.perform(withAuth(MockMvcRequestBuilders.delete(URL + "/" + student.getId())))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void deleteStudentUnknown() throws Exception {
        // GIVEN aucun étudiant pour cet id
        // WHEN on tente de le supprimer
        // THEN 404
        mockMvc.perform(withAuth(MockMvcRequestBuilders.delete(URL + "/" + UNKNOWN_ID)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

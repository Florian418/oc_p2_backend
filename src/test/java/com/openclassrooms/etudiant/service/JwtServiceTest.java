package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de JwtService : génération, lecture et validation des
 * tokens JWT. Aucune dépendance à mocker (la classe n'a pas de collaborateur
 * injecté), donc pas besoin de Mockito ici.
 */
public class JwtServiceTest {
    private static final String LOGIN = "LOGIN";
    private static final String OTHER_LOGIN = "OTHER_LOGIN";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        user = new User();
        user.setLogin(LOGIN);
    }

    @Test
    public void test_generateToken_returns_non_empty_token() {
        // WHEN on génère un token pour un utilisateur valide
        String token = jwtService.generateToken(user);

        // THEN le token n'est ni nul ni vide
        assertThat(token).isNotBlank();
    }

    @Test
    public void test_extractUsername_returns_original_login() {
        // GIVEN un token généré pour un login donné
        String token = jwtService.generateToken(user);

        // WHEN on extrait le username du token
        String extractedLogin = jwtService.extractUsername(token);

        // THEN on retrouve le login d'origine
        assertThat(extractedLogin).isEqualTo(LOGIN);
    }

    @Test
    public void test_validateToken_same_user_returns_true() {
        // GIVEN un token généré pour un utilisateur
        String token = jwtService.generateToken(user);

        // WHEN on valide ce token avec le même utilisateur
        boolean valid = jwtService.validateToken(token, user);

        // THEN le token est valide
        assertThat(valid).isTrue();
    }

    @Test
    public void test_validateToken_different_user_returns_false() {
        // GIVEN un token généré pour un utilisateur
        String token = jwtService.generateToken(user);
        User otherUser = new User();
        otherUser.setLogin(OTHER_LOGIN);

        // WHEN on valide ce token avec un utilisateur différent
        boolean valid = jwtService.validateToken(token, otherUser);

        // THEN le token n'est pas considéré comme valide
        assertThat(valid).isFalse();
    }
}

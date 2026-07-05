package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de CustomUserDetailService : vérifie le chargement d'un
 * utilisateur par son login pour l'authentification Spring Security.
 */
@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {
    private static final String LOGIN = "LOGIN";

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    public void test_loadUserByUsername_existing_login_returns_user() {
        // GIVEN un login qui existe en base
        User user = new User();
        user.setLogin(LOGIN);
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));

        // WHEN on charge l'utilisateur par ce login
        UserDetails userDetails = customUserDetailService.loadUserByUsername(LOGIN);

        // THEN le UserDetails renvoyé correspond bien au User trouvé en base
        assertThat(userDetails).isEqualTo(user);
    }

    @Test
    public void test_loadUserByUsername_unknown_login_throws_UsernameNotFoundException() {
        // GIVEN un login qui n'existe pas en base
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

        // THEN une UsernameNotFoundException est levée
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailService.loadUserByUsername(LOGIN));
    }
}

package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de JwtAuthFilter, centrés sur la sécurité : requêtes sans
 * token et avec un token invalide, deux cas qui surviendront forcément en
 * usage réel (token expiré, absence d'authentification) et qui n'étaient
 * couverts par aucun test jusqu'ici (les tests d'intégration existants
 * n'utilisent que des tokens valides).
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailService customUserDetailService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    public void clearSecurityContext() {
        // Le SecurityContextHolder est un état global (ThreadLocal) : on le
        // nettoie après chaque test pour ne pas polluer les tests suivants.
        SecurityContextHolder.clearContext();
    }

    @Test
    public void test_shouldNotFilter_login_route_is_excluded() {
        // GIVEN une requête vers la route publique /api/login
        when(request.getRequestURI()).thenReturn("/api/login");

        // THEN elle est exclue du filtre JWT
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isTrue();
    }

    @Test
    public void test_shouldNotFilter_protected_route_is_not_excluded() {
        // GIVEN une requête vers une route protégée
        when(request.getRequestURI()).thenReturn("/api/students");

        // THEN elle n'est pas exclue, le filtre doit s'appliquer
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isFalse();
    }

    @Test
    public void test_doFilterInternal_no_authorization_header_passes_through_unauthenticated() throws Exception {
        // GIVEN une requête sans header Authorization
        when(request.getHeader("Authorization")).thenReturn(null);

        // WHEN le filtre traite la requête
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // THEN la requête continue sans authentification, aucune erreur renvoyée
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void test_doFilterInternal_invalid_token_returns_401_and_stops_the_chain() throws Exception {
        // GIVEN un token invalide (expiré ou signature incorrecte)
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("invalid"));

        // WHEN le filtre traite la requête
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // THEN une erreur 401 est renvoyée et la chaîne de filtres est stoppée
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou expiré");
        verify(filterChain, never()).doFilter(any(), any());
    }
}

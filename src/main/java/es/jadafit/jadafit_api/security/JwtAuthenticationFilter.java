package es.jadafit.jadafit_api.security;

import es.jadafit.jadafit_api.model.UserSession;
import es.jadafit.jadafit_api.repository.UserRepository;
import es.jadafit.jadafit_api.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserSessionRepository sessionRepository, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        try {
            if (
                    authHeader != null
                            && authHeader.startsWith("Bearer ")
                            && SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                String token = authHeader.substring(7);

                if (jwtUtils.validateToken(token)) {
                    String userId = jwtUtils.getSubjectFromToken(token);
                    String sessionId = jwtUtils.getSessionIdFromToken(token);

                    if (sessionId != null && isSessionValid(userId, sessionId)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        Collections.emptyList()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        System.err.println("[JWT] Invalid or expired session for user " + userId);
                    }
                } else {
                    System.err.println("[JWT] Token validation failed");
                }
            }
        } catch (Exception e) {
            System.err.println("[JWT] Error: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSessionValid(String userId, String sessionId) {
        Optional<UserSession> session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId);
        if (session.isPresent()) {
            UserSession s = session.get();
            if (!s.getUserId().toString().equals(userId)) return false;
            if (s.getExpiresAt().isBefore(LocalDateTime.now())) {
                s.setIsActive(false);
                sessionRepository.save(s);
                return false;
            }
            return true;
        }

        try {
            var userOpt = userRepository.findById(UUID.fromString(userId));
            if (userOpt.isPresent()) {
                String oldSessionId = userOpt.get().getSessionId();
                if (sessionId.equals(oldSessionId)) {
                    System.err.println("[JWT] Migrating session to user_sessions table for user " + userId);
                    UserSession migrated = UserSession.builder()
                            .userId(UUID.fromString(userId))
                            .sessionId(sessionId)
                            .createdAt(LocalDateTime.now())
                            .expiresAt(LocalDateTime.now().plusDays(30))
                            .isActive(true)
                            .build();
                    sessionRepository.save(migrated);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[JWT] Fallback check failed: " + e.getMessage());
        }

        return false;
    }
}

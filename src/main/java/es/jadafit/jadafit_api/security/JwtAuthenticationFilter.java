package es.jadafit.jadafit_api.security;

import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
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

                    Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
                    if (userOpt.isPresent()) {
                        String dbSessionId = userOpt.get().getSessionId();

                        if (sessionId != null && sessionId.equals(dbSessionId)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userId,
                                            null,
                                            Collections.emptyList()
                                    );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            System.err.println("[JWT] SessionId mismatch for user " + userId
                                    + ": token_sid=" + sessionId
                                    + ", db_sid=" + dbSessionId);

                            if (dbSessionId == null) {
                                System.err.println("[JWT] DB sessionId is null, accepting token anyway");
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userId,
                                                null,
                                                Collections.emptyList()
                                        );
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        }
                    } else {
                        System.err.println("[JWT] User not found: " + userId);
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
}
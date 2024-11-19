package org.openapitools.util;

import org.openapitools.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        Long userId = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractEmail(jwt);
            userId = jwtUtil.extractUserId(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername(), userId)) {
                String requestURI = request.getRequestURI();
                String[] uriParts = requestURI.split("/");
                Long userIdFromUri = null;

                System.out.println("Request URI: " + requestURI);
                System.out.println("User ID from token: " + userId);

                try {
                    userIdFromUri = Long.parseLong(uriParts[uriParts.length - 1]);
                    System.out.println("User ID from URI: " + userIdFromUri);

                    if (!userId.equals(userIdFromUri)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "El ID del usuario no coincide con el token.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El ID del usuario en la URL no es válido.");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Asignando Authorities en JwtRequestFilter: " + authToken.getAuthorities());
            }
        }
        chain.doFilter(request, response);
    }
}

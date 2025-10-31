package com.cbs.CabBookingSystem.filter;

import com.cbs.CabBookingSystem.service.UserDetailsServiceImpl;
import com.cbs.CabBookingSystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Lombok annotation for constructor injection of final fields
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check for Authorization Header and "Bearer " prefix
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT (token starts after "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 3. Extract email (username) from JWT
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Log the exception, but continue the filter chain
            System.err.println("JWT processing error: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Validate user and security context
        // Check if email is found and if the user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Load user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate token expiration and signature
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // 7. Create authentication object
                // The principal is the UserDetails, credentials are null (as token is the credential), and authorities are loaded from UserDetails.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 8. Set details (allows Spring Security to know IP, session ID, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Set authentication in the Security Context (This is the "login")
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Pass the request/response to the next filter in the chain (or the controller)
        filterChain.doFilter(request, response);
    }
}
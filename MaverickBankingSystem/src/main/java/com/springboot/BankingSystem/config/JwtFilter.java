package com.springboot.BankingSystem.config;

import com.springboot.BankingSystem.service.MyUserDetailsService;
import com.springboot.BankingSystem.utility.JwtUtility;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final MyUserDetailsService myUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Let's read the token from the header (Authorization)
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = "";

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.split(" ")[1];
            try {
                username = jwtUtility.extractUsername(token);
            }
            catch (JwtException | IllegalArgumentException e) {
                // expired, tampered or malformed token, continue without login so the response is 401 instead of 500
                log.warn("Token is expired or malformed");
                username = null;
            }
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                if(jwtUtility.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            catch (UsernameNotFoundException e) {
                log.warn("User is inactive");
                // user is deleted or inactive, continue without login so the response is 401
            }
        }

        filterChain.doFilter(request, response);
    }
}
package com.backend.smarthire.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. Look for the "Authorization" header in the incoming request
        String authHeader = request.getHeader("Authorization");

        // 2. Check if the header contains a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token=authHeader.substring(7);// Remove "Bearer " to get the raw token
            try{
                // 3. Extract the data from the token
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // 4. Tell Spring Security that this user is officially authenticated!
                UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(
                        email,null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (Exception e){
                // If the token is expired or fake, we do nothing and let Spring block them
                System.out.println("Invalid JWT Token!");
            }
        }
        chain.doFilter(request,response);
    }

}

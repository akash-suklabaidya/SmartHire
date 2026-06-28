package com.backend.smarthire.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET = "SmartHireSuperSecretKeyForJwtGeneration12345!";
    private final Key key=Keys.hmacShaKeyFor(SECRET.getBytes());

    // Set the token to expire in 24 hours (86,400,000 milliseconds)
    private final long expirationTime = 86400000;

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email) // The main identity of the user
                .claim("role", role) // We embed the role so the frontend knows what they can do
                .setIssuedAt(new Date()) // Current time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Expiry time
                .signWith(key) // We sign it with our secret key so hackers can't forge it!
                .compact();
    }
    public String extractEmail(String token) {
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(key) // Uses the same key method from your setup
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String extractRole(String token) {
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
}

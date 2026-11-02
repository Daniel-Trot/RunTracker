package runtracker.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtServiceTest {

    @Test
    void generateTokenContainsCorrectClaims() {

        // GIVEN
        JwtService jwtService = new JwtService();

        String testSecret =
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                testSecret
        );

        // WHEN
        String token = jwtService.generateToken("test@gmail.com");

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(testSecret)
        );

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // THEN
        assertEquals("test@gmail.com", claims.getSubject());

        assertTrue(
                claims.getExpiration().after(claims.getIssuedAt())
        );
    }
}
package id.ac.ui.cs.advprog.chat.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private String secret = Base64.getEncoder().encodeToString("mySuperSecretKey123456789012345678".getBytes()); // 32+ chars

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
    }

    private String generateToken(String userId, long validityMillis) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMillis))
                .signWith(jwtUtils.key(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void testValidateJwtToken_validToken_returnsTrue() {
        String token = generateToken("123", 60000);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testValidateJwtToken_expiredToken_returnsFalse() {
        String token = generateToken("123", -1000); // already expired
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testValidateJwtToken_malformedToken_returnsFalse() {
        String token = "this.is.not.a.jwt";
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testGetUserIdFromJwtToken() {
        String token = generateToken("456", 60000);
        String userId = jwtUtils.getUserIdFromJwtToken(token);
        assertEquals("456", userId);
    }

    @Test
    void testGetClaimsFromJwtToken() {
        String token = generateToken("789", 60000);
        assertEquals("789", jwtUtils.getClaimsFromJwtToken(token).getSubject());
    }

    @Test
    void testGetRolesFromToken_returnsDefaultRole() {
        String token = generateToken("111", 60000);
        List<String> roles = jwtUtils.getRolesFromToken(token);
        assertEquals(List.of("ROLE_USER"), roles);
    }

    @Test
    void testGetUsernameFromToken_success() {
        String token = generateToken("999", 60000);
        String username = jwtUtils.getUsernameFromToken(token);
        assertEquals("999", username);
    }

    @Test
    void testGetUsernameFromToken_invalidToken_returnsNull() {
        String token = "invalid.token";
        String username = jwtUtils.getUsernameFromToken(token);
        assertNull(username);
    }
}

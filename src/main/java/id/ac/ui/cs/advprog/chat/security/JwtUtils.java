package id.ac.ui.cs.advprog.chat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Use the same secret as auth-profile service
    @Value("${pandacare.app.jwtSecret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
        }

        return false;
    }

    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }

    /**
     * Extract roles from JWT token claims
     * Since the auth-profile JWT only contains userId, we need to make a call to get roles
     * For now, return default roles based on user type
     */
    public List<String> getRolesFromToken(String token) {
        try {
            // Since our JWT from auth-profile doesn't contain roles as separate claims,
            // we'll return a default role. In a real implementation, you might want to
            // make a call to auth-profile service to get user details
            return Arrays.asList("ROLE_USER");
        } catch (Exception e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return Arrays.asList("ROLE_USER");
        }
    }

    /**
     * Extract username from JWT token claims
     * Since our JWT only contains userId as subject, we'll use that
     */
    public String getUsernameFromToken(String token) {
        try {
            // Our JWT uses userId as subject, so we'll return that as username
            return getUserIdFromJwtToken(token);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
}
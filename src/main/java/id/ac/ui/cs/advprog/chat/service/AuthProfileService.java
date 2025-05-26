package id.ac.ui.cs.advprog.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Service for making authenticated calls to the auth-profile service
 */
@Service
public class AuthProfileService {

    private static final Logger logger = LoggerFactory.getLogger(AuthProfileService.class);

    private final RestTemplate restTemplate;

    @Value("${authprofile.service.url:http://localhost:8081/api}")
    private String authProfileUrl;

    @Autowired
    public AuthProfileService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get user name by ID with authentication
     */
    public String getUserName(Long userId) {
        return getUserName(userId, getCurrentAuthToken());
    }

    /**
     * Get user name by ID with provided auth token
     */
    public String getUserName(Long userId, String authToken) {
        if (authToken == null) {
            logger.warn("No auth token provided for getUserName");
            return "User " + userId;
        }

        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Try regular user endpoint first
            String url = authProfileUrl + "/user/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("name");
            }
        } catch (Exception e) {
            logger.debug("Error getting user name for {}: {}", userId, e.getMessage());
        }

        return "User " + userId;
    }

    /**
     * Check if user is a caregiver
     */
    public boolean isCaregiver(Long userId) {
        return isCaregiver(userId, getCurrentAuthToken());
    }

    /**
     * Check if user is a caregiver with provided auth token
     */
    public boolean isCaregiver(Long userId, String authToken) {
        if (authToken == null) {
            logger.warn("No auth token provided for isCaregiver check");
            return false;
        }

        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = authProfileUrl + "/caregiver/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("User {} is a caregiver", userId);
                return true;
            }
        } catch (Exception e) {
            logger.debug("User {} is not a caregiver: {}", userId, e.getMessage());
        }

        return false;
    }

    /**
     * Get user profile by ID
     */
    public Map<String, Object> getUserProfile(Long userId) {
        return getUserProfile(userId, getCurrentAuthToken());
    }

    /**
     * Get user profile by ID with provided auth token
     */
    public Map<String, Object> getUserProfile(Long userId, String authToken) {
        if (authToken == null) {
            logger.warn("No auth token provided for getUserProfile");
            return null;
        }

        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = authProfileUrl + "/user/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            logger.debug("Error getting user profile for {}: {}", userId, e.getMessage());
        }

        return null;
    }

    /**
     * Get caregiver profile by ID
     */
    public Map<String, Object> getCaregiverProfile(Long caregiverId) {
        return getCaregiverProfile(caregiverId, getCurrentAuthToken());
    }

    /**
     * Get caregiver profile by ID with provided auth token
     */
    public Map<String, Object> getCaregiverProfile(Long caregiverId, String authToken) {
        if (authToken == null) {
            logger.warn("No auth token provided for getCaregiverProfile");
            return null;
        }

        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = authProfileUrl + "/caregiver/" + caregiverId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            logger.debug("Error getting caregiver profile for {}: {}", caregiverId, e.getMessage());
        }

        return null;
    }

    /**
     * Get auth token from current HTTP request context
     */
    private String getCurrentAuthToken() {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get auth token from request context: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Create HTTP headers with authentication
     */
    private HttpHeaders createAuthHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        if (authToken != null) {
            headers.set("Authorization", authToken);
        }
        return headers;
    }
}
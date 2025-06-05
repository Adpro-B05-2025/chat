package id.ac.ui.cs.advprog.chat.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void testUserPrincipalFieldsAndMethods() {
        Long id = 1L;
        String username = "user1";
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        UserPrincipal principal = new UserPrincipal(id, username, authorities);

        // Principal
        assertEquals(username, principal.getName());

        // UserDetails
        assertEquals(username, principal.getUsername());
        assertEquals(authorities, principal.getAuthorities());
        assertNull(principal.getPassword()); // JWT-based login doesn't need this
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());

        // Custom method
        assertEquals(id, principal.getId());

        // toString
        String result = principal.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='user1'"));
        assertTrue(result.contains("ROLE_USER"));
    }
}

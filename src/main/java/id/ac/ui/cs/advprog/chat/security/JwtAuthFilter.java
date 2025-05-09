package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.dto.TokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired private RestTemplate restTemplate;
    // sesuaikan alamat jika auth-profile di host lain
    private final String authValidateUrl = "http://localhost:8081/api/auth/validate";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", header);
            HttpEntity<Void> ent = new HttpEntity<>(h);
            try {
                ResponseEntity<TokenValidationResponse> resp =
                        restTemplate.exchange(authValidateUrl, HttpMethod.POST, ent, TokenValidationResponse.class);

                TokenValidationResponse tvr = resp.getBody();
                if (resp.getStatusCode().is2xxSuccessful() && tvr != null && tvr.isValid()) {
                    var authorities = tvr.getRoles().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    UserPrincipal principal = new UserPrincipal(tvr.getUserId(), tvr.getUsername(), authorities);
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) { /* gagal validate → lanjut tanpa auth */ }
        }
        chain.doFilter(req, res);
    }
}

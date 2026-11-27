
package ma.ensias.devpilot.gitserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${devpilot.security.api-key:}") String apiKey
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/mcp", "/mcp/**").permitAll()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(
                        new ApiKeyFilter(apiKey),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private static class ApiKeyFilter extends OncePerRequestFilter {

        private final String expectedApiKey;

        private ApiKeyFilter(String expectedApiKey) {
            this.expectedApiKey = expectedApiKey;
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getServletPath();
            return !path.equals("/mcp") && !path.startsWith("/mcp/");
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            System.out.println("=== API KEY FILTER EXECUTED ===");
            System.out.println("Request path: " + request.getServletPath());

            String providedKey = request.getHeader(HttpHeaders.AUTHORIZATION);

            boolean valid = expectedApiKey != null
                    && !expectedApiKey.isBlank()
                    && providedKey != null
                    && MessageDigest.isEqual(
                    expectedApiKey.getBytes(StandardCharsets.UTF_8),
                    providedKey.getBytes(StandardCharsets.UTF_8)
            );

            System.out.println("API key valid: " + valid);

            if (!valid) {
                System.out.println("Access denied: invalid or missing API key");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(
                        "Invalid or missing MCP API key"
                );
                return;
            }

            System.out.println("API key accepted. Continuing request.");

            filterChain.doFilter(request, response);
        }
    }
}

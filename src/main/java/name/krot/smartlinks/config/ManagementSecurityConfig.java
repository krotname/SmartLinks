package name.krot.smartlinks.config;

import name.krot.smartlinks.security.ManagementOriginFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
public class ManagementSecurityConfig {
    @Bean
    PasswordEncoder ownerPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService ownerUsers(@Value("${smartlinks.owner.username:}") String username,
                                 @Value("${smartlinks.owner.password-hash:}") String hash) {
        return candidate -> {
            if (username.isBlank() || hash.isBlank() || !username.equals(candidate)) {
                throw new UsernameNotFoundException("Owner access is not configured or credentials are invalid");
            }
            return User.withUsername(username).password(hash).roles("OWNER").build();
        };
    }

    @Bean
    @Order(1)
    SecurityFilterChain managementSecurity(HttpSecurity http,
            @Value("${smartlinks.owner.host:links.n-0.ru}") String host) throws Exception {
        http.securityMatcher("/manage", "/manage/**")
                .addFilterBefore(new ManagementOriginFilter(host), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/manage/login", "/manage/csrf", "/manage/login.js", "/manage/style.css").permitAll()
                        .anyRequest().hasRole("OWNER"))
                .formLogin(form -> form.loginPage("/manage/login")
                        .loginProcessingUrl("/manage/login")
                        .defaultSuccessUrl("/manage/", true)
                        .failureUrl("/manage/login?error"))
                .logout(logout -> logout.logoutUrl("/manage/logout").logoutSuccessUrl("/manage/login"))
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/manage/api/")) response.sendError(401);
                    else response.sendRedirect("/manage/login");
                }))
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'none'; script-src 'self'; style-src 'self'; connect-src 'self'; "
                        + "base-uri 'none'; form-action 'self'; frame-ancestors 'none'; object-src 'none'")));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain legacySecurity(HttpSecurity http) throws Exception {
        // Legacy writes are authenticated by ApiKeyAuthenticationFilter, without browser sessions.
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

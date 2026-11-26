package kz.iitu.springlab.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()      // DEV ONLY
                        .requestMatchers("/", "/login", "/css/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                        .requestMatchers("/books/**").hasAnyRole("LIBRARIAN", "ADMIN")   // create, edit
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))   // DEV ONLY
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))  // DEV ONLY
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
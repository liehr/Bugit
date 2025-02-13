package de.tudl.playground.bugit.config;

import lombok.SneakyThrows;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Setze hier keinen manuellen Header-Writer mehr ein, da CORS global konfiguriert wird
                .exceptionHandling(c -> c
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Aktiviert CORS und greift auf deinen globalen CorsConfigurationSource zu
                .cors(Customizer.withDefaults())
                // Deaktiviert CSRF, da du stateless (z.B. JWT) arbeitest
                .csrf(AbstractHttpConfigurer::disable)
                // Setzt den Session-Management-Modus auf stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Erlaubt alle OPTIONS-Anfragen, damit der CORS-Preflight nicht blockiert wird
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Erlaubt explizit die Registrierungs- und Login-Endpunkte sowie den Status
                        .requestMatchers("/api/user/register", "/api/user/login", "/api/status/**").permitAll()
                        // Alle weiteren Endpunkte müssen authentifiziert sein
                        .anyRequest().authenticated())
                // Optional: Aktiviert HTTP Basic (nur wenn benötigt)
                .httpBasic(Customizer.withDefaults())
                // Fügt deinen JWT-Filter vor dem UsernamePasswordAuthenticationFilter ein
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @SneakyThrows
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    // Registriert deinen RequestThrottleFilter für alle API-Endpunkte.
    // Da du im Filter selbst OPTIONS-Anfragen jetzt überspringst,
    // wird der CORS-Preflight nicht blockiert.
    @Bean
    public FilterRegistrationBean<RequestThrottleFilter> requestThrottleFilter() {
        FilterRegistrationBean<RequestThrottleFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestThrottleFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

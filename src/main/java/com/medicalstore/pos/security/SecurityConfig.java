package com.medicalstore.pos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${CORS_ALLOWED_ORIGINS:0.0.0.0}")
    private String corsAllowedOrigins;
    
    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins if CORS_ALLOWED_ORIGINS is set to "*" or "0.0.0.0"
        if ("*".equals(corsAllowedOrigins) || "0.0.0.0".equals(corsAllowedOrigins)) {
            configuration.addAllowedOriginPattern("*");
        } else {
            // Parse CORS origins from environment variable (comma-separated)
            List<String> allowedOrigins = Arrays.asList(corsAllowedOrigins.split(","));
            configuration.setAllowedOrigins(allowedOrigins);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-ui/index.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/api-docs/**",
                    "/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/configuration/**",
                    "/favicon.ico"
                ).permitAll()
                // Reports endpoints - ANALYST, MANAGER, and ADMIN (MUST come before /api/admin/**)
                .requestMatchers("/api/admin/reports/**").hasAnyRole("ADMIN", "ANALYST", "MANAGER")
                // Audit endpoints - ADMIN only (MUST come before /api/admin/**)
                .requestMatchers("/api/admin/audit/**").hasRole("ADMIN")
                // Admin endpoints - ADMIN only (general catch-all for other admin endpoints)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Billing endpoints
                // Purchase History (GET all bills) - MANAGER, CASHIER, and ADMIN
                .requestMatchers("/api/cashier/bills").hasAnyRole("ADMIN", "CASHIER", "MANAGER")
                // Bill viewing endpoints - MANAGER can view bills (for purchase history details)
                // Note: Manager can view bills but frontend restricts creation/modification
                .requestMatchers("/api/cashier/bills/**").hasAnyRole("ADMIN", "CASHIER", "MANAGER")
                .requestMatchers("/api/cashier/returns/**").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT")
                // Inventory endpoints - STOCK_MONITOR and ADMIN
                .requestMatchers("/api/pharmacist/batches/**").hasAnyRole("ADMIN", "STOCK_MONITOR")
                // Medicines endpoints - STOCK_KEEPER and ADMIN
                .requestMatchers("/api/pharmacist/medicines/**").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}


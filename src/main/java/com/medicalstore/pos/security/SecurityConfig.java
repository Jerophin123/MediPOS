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
import org.springframework.http.HttpMethod;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
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
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
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
                // ============================================
                // REPORTS ENDPOINTS
                // ============================================
                // Reports - ANALYST, MANAGER, and ADMIN (MUST come before /api/admin/**)
                .requestMatchers("/api/admin/reports/**").hasAnyRole("ADMIN", "ANALYST", "MANAGER")
                
                // ============================================
                // AUDIT LOGS ENDPOINTS
                // ============================================
                // Audit logs - ADMIN only (MUST come before /api/admin/**)
                .requestMatchers("/api/admin/audit/**").hasRole("ADMIN")
                
                // ============================================
                // USER MANAGEMENT ENDPOINTS
                // ============================================
                // User management - ADMIN only (MUST come before /api/admin/**)
                .requestMatchers("/api/admin/users/**").hasRole("ADMIN")
                
                // ============================================
                // ADMIN ENDPOINTS (Catch-all)
                // ============================================
                // All other admin endpoints - ADMIN only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // ============================================
                // BILLING ENDPOINTS
                // ============================================
                // Bill creation and modification - CASHIER and ADMIN only
                // MANAGER can only view bills (GET requests), not create/cancel
                .requestMatchers(HttpMethod.POST, "/api/cashier/bills").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/api/cashier/bills/*/cancel").hasAnyRole("ADMIN", "CASHIER")
                // Bill viewing - CASHIER, MANAGER, CUSTOMER_SUPPORT (for returns), and ADMIN
                // CUSTOMER_SUPPORT needs to view bills to process returns
                .requestMatchers(HttpMethod.GET, "/api/cashier/bills").hasAnyRole("ADMIN", "CASHIER", "MANAGER", "CUSTOMER_SUPPORT")
                .requestMatchers(HttpMethod.GET, "/api/cashier/bills/*").hasAnyRole("ADMIN", "CASHIER", "MANAGER", "CUSTOMER_SUPPORT")
                .requestMatchers(HttpMethod.GET, "/api/cashier/bills/number/*").hasAnyRole("ADMIN", "CASHIER", "MANAGER", "CUSTOMER_SUPPORT")
                
                // ============================================
                // RETURNS ENDPOINTS
                // ============================================
                // Returns viewing - CUSTOMER_SUPPORT, MANAGER, and ADMIN (MUST come first)
                .requestMatchers(HttpMethod.GET, "/api/cashier/returns").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/cashier/returns/*").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/cashier/returns/bill/*").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT", "MANAGER")
                // Returns processing - CUSTOMER_SUPPORT and ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/cashier/returns").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT")
                
                // ============================================
                // BATCH ENDPOINTS
                // ============================================
                // Batch read operations for billing - CASHIER needs to get batches for medicines (MUST come first)
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/batches/medicine/*").hasAnyRole("ADMIN", "STOCK_MONITOR", "STOCK_KEEPER", "CASHIER")
                // Batch read operations - STOCK_MONITOR, STOCK_KEEPER, and ADMIN (MUST come before write operations)
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/batches").hasAnyRole("ADMIN", "STOCK_MONITOR", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/batches/*").hasAnyRole("ADMIN", "STOCK_MONITOR", "STOCK_KEEPER")
                // Batch write operations (POST, PUT, DELETE) - STOCK_KEEPER and ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/pharmacist/batches").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.POST, "/api/pharmacist/batches/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.PUT, "/api/pharmacist/batches/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.DELETE, "/api/pharmacist/batches/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                
                // ============================================
                // MEDICINE ENDPOINTS
                // ============================================
                // Medicine read operations for billing - CASHIER needs search and lookup (MUST come first)
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/medicines/barcode/*").hasAnyRole("ADMIN", "STOCK_KEEPER", "CASHIER")
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/medicines/search").hasAnyRole("ADMIN", "STOCK_KEEPER", "CASHIER")
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/medicines/*").hasAnyRole("ADMIN", "STOCK_KEEPER", "CASHIER")
                // Medicine read operations - STOCK_KEEPER and ADMIN (MUST come before write operations)
                .requestMatchers(HttpMethod.GET, "/api/pharmacist/medicines").hasAnyRole("ADMIN", "STOCK_KEEPER")
                // Medicine write operations (POST, PUT, DELETE) - STOCK_KEEPER and ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/pharmacist/medicines").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.POST, "/api/pharmacist/medicines/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.PUT, "/api/pharmacist/medicines/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .requestMatchers(HttpMethod.DELETE, "/api/pharmacist/medicines/*").hasAnyRole("ADMIN", "STOCK_KEEPER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}


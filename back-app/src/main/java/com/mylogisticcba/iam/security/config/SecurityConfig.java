package com.mylogisticcba.iam.security.config;

import com.mylogisticcba.iam.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Qualifier("tenantProvider")
    private final AuthenticationProvider authProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;

    /*  para produccion
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return  http
                .csrf(csrf->
                        csrf.disable())//deshabilitamos proteccion por defecto(cross-site reques forgery)
                .authorizeHttpRequests(authRequest ->
                  authRequest
                          .requestMatchers("/auth/**").permitAll()
                          .anyRequest().authenticated()
                    )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();


    }

*/ // para desarrollo
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable()) // Deshabilitado para todo
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()) // Permite el uso de frames (H2 necesita esto)
                )
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers("/auth/**",
                                        "/h2-console/**"
                                      ).permitAll() // H2 y Auth libres
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }



}

package com.nestly.config;

import com.nestly.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtFilter;
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain chain(HttpSecurity http)throws Exception{return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/actuator/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll().anyRequest().authenticated()).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();}
}


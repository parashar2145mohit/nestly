package com.nestly.security;
import com.nestly.auth.Role;
public record NestlyPrincipal(Long userId, Long societyId, String email, Role role) {}


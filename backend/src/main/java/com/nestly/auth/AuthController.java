package com.nestly.auth;

import com.nestly.security.JwtService;
import com.nestly.security.NestlyPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  public record LoginRequest(@Email String email,@NotBlank String password){}
  public record AuthResponse(String token,Long userId,Long societyId,String name,Role role){}
  @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request){
    AppUser u=users.findByEmailIgnoreCase(request.email()).filter(AppUser::isEnabled).filter(x->encoder.matches(request.password(),x.getPasswordHash())).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
    return new AuthResponse(jwt.issue(u),u.getId(),u.getSocietyId(),u.getName(),u.getRole());
  }
  @GetMapping("/me") public AuthResponse me(org.springframework.security.core.Authentication authentication){
    NestlyPrincipal p=(NestlyPrincipal)authentication.getPrincipal();
    AppUser u=users.findById(p.userId()).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
    return new AuthResponse(null,u.getId(),u.getSocietyId(),u.getName(),u.getRole());
  }
}

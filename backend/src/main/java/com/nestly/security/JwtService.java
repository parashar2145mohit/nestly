package com.nestly.security;

import com.nestly.auth.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key;
  public JwtService(@Value("${nestly.jwt-secret}") String secret) { this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
  public String issue(AppUser user) {
    Instant now=Instant.now();
    return Jwts.builder().subject(user.getEmail()).claim("uid",user.getId()).claim("sid",user.getSocietyId()).claim("role",user.getRole().name()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(86400))).signWith(key).compact();
  }
  public NestlyPrincipal parse(String token) {
    Claims c=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    Number sid=c.get("sid",Number.class), uid=c.get("uid",Number.class);
    return new NestlyPrincipal(uid.longValue(),sid==null?null:sid.longValue(),c.getSubject(),com.nestly.auth.Role.valueOf(c.get("role",String.class)));
  }
}


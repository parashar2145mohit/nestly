package com.nestly.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component @RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
    String header=req.getHeader(HttpHeaders.AUTHORIZATION);
    if(header!=null&&header.startsWith("Bearer ")) try {
      NestlyPrincipal p=jwt.parse(header.substring(7));
      var auth=new UsernamePasswordAuthenticationToken(p,null,List.of(new SimpleGrantedAuthority("ROLE_"+p.role().name())));
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch(Exception ignored) { SecurityContextHolder.clearContext(); }
    chain.doFilter(req,res);
  }
}


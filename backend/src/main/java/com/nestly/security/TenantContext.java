package com.nestly.security;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
@Component public class TenantContext {
  public NestlyPrincipal principal(){Object p=SecurityContextHolder.getContext().getAuthentication().getPrincipal();if(p instanceof NestlyPrincipal n)return n;throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);}
  public Long societyId(){Long id=principal().societyId();if(id==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Society context required");return id;}
}


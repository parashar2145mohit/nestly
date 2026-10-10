package com.nestly.common;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
  record ErrorBody(Instant timestamp,int status,String message,Map<String,String> errors){}
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ErrorBody> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(new ErrorBody(Instant.now(),e.getStatusCode().value(),e.getReason(),Map.of()));}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e){Map<String,String> m=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->m.put(x.getField(),x.getDefaultMessage()));return ResponseEntity.badRequest().body(new ErrorBody(Instant.now(),400,"Validation failed",m));}
}

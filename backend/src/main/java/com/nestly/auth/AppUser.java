package com.nestly.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="app_user") @Getter @Setter @NoArgsConstructor
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="society_id") private Long societyId;
  @Enumerated(EnumType.STRING) private Role role;
  private String name;
  private String email;
  private String phone;
  @Column(name="password_hash") private String passwordHash;
  private boolean enabled = true;
  @Column(name="created_at", insertable=false, updatable=false) private Instant createdAt;
}


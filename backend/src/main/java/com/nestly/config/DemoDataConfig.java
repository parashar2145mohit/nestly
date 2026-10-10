package com.nestly.config;

import com.nestly.auth.AppUser;
import com.nestly.auth.Role;
import com.nestly.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DemoDataConfig {
  private final JdbcTemplate jdbc;
  private final UserRepository users;
  private final PasswordEncoder encoder;

  @Bean
  CommandLineRunner seedNestlyDemo() {
    return args -> {
      Long societyId = jdbc.query("select id from society where name = ? limit 1",
          rs -> rs.next() ? rs.getLong(1) : null, "Oakwood Residency");
      if (societyId == null) {
        jdbc.update("insert into society(name,address,subscription_plan) values (?,?,?)",
            "Oakwood Residency", "Sector 45, Gurugram", "PRO");
        societyId = jdbc.queryForObject("select id from society where name = ?", Long.class, "Oakwood Residency");
      }
      AppUser admin = createUser("admin@nestly.local", "Aarav Sharma", Role.SOCIETY_ADMIN, societyId);
      createUser("guard@nestly.local", "Rakesh Kumar", Role.SECURITY_GUARD, societyId);
      addResident("resident@nestly.local", "Aisha Mehta", "98765 41001", "B", "204", true, societyId);
      addResident("kabir@nestly.local", "Kabir Singh", "98765 41002", "A", "110", false, societyId);
      addResident("neha@nestly.local", "Neha Kapoor", "98765 41003", "C", "302", true, societyId);
      addResident("rohan@nestly.local", "Rohan Das", "98765 41004", "A", "405", true, societyId);
      addResident("ishita@nestly.local", "Ishita Jain", "98765 41005", "B", "108", false, societyId);
      addResident("vikram@nestly.local", "Vikram Rao", "98765 41006", "C", "201", true, societyId);
      seedNotices(societyId, admin.getId());
    };
  }

  private AppUser createUser(String email, String name, Role role, Long societyId) {
    AppUser existing = users.findByEmailIgnoreCase(email).orElse(null);
    if (existing != null) return existing;
    AppUser user = new AppUser();
    user.setEmail(email);
    user.setName(name);
    user.setRole(role);
    user.setSocietyId(societyId);
    user.setPhone("9999999999");
    user.setPasswordHash(encoder.encode("Nestly@123"));
    user.setEnabled(true);
    return users.save(user);
  }

  private void addResident(String email, String name, String phone, String blockName, String flatNumber,
                           boolean owner, Long societyId) {
    AppUser user = createUser(email, name, Role.RESIDENT, societyId);
    jdbc.update("update app_user set phone=? where id=?", phone, user.getId());
    Long blockId = jdbc.query("select id from block where society_id=? and block_name=? limit 1",
        rs -> rs.next() ? rs.getLong(1) : null, societyId, blockName);
    if (blockId == null) {
      jdbc.update("insert into block(society_id,block_name) values (?,?)", societyId, blockName);
      blockId = jdbc.queryForObject("select id from block where society_id=? and block_name=?", Long.class, societyId, blockName);
    }
    Long flatId = jdbc.query("select id from flat where block_id=? and flat_number=? limit 1",
        rs -> rs.next() ? rs.getLong(1) : null, blockId, flatNumber);
    if (flatId == null) {
      jdbc.update("insert into flat(society_id,block_id,flat_number,flat_type) values (?,?,?,?)",
          societyId, blockId, flatNumber, "2BHK");
      flatId = jdbc.queryForObject("select id from flat where block_id=? and flat_number=?", Long.class, blockId, flatNumber);
    }
    Integer count = jdbc.queryForObject("select count(*) from resident where user_id=?", Integer.class, user.getId());
    if (count != null && count == 0) {
      jdbc.update("insert into resident(society_id,user_id,flat_id,is_owner) values (?,?,?,?)",
          societyId, user.getId(), flatId, owner);
    }
  }

  private void seedNotices(Long societyId, Long adminId) {
    Integer count = jdbc.queryForObject("select count(*) from notice where society_id=?", Integer.class, societyId);
    if (count != null && count > 0) return;
    jdbc.update("insert into notice(society_id,title,body,posted_by) values (?,?,?,?)", societyId,
        "Annual general meeting", "Community hall · Sunday, 11:00 AM. All residents are requested to attend.", adminId);
    jdbc.update("insert into notice(society_id,title,body,posted_by) values (?,?,?,?)", societyId,
        "Water supply maintenance", "Supply will be paused from 2:00–4:00 PM tomorrow.", adminId);
    jdbc.update("insert into notice(society_id,title,body,posted_by) values (?,?,?,?)", societyId,
        "Diwali celebration registrations", "Register your family before 18 October at the society office.", adminId);
  }
}

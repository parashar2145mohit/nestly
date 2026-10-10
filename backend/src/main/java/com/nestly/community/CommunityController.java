package com.nestly.community;

import com.nestly.security.NestlyPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
  private final JdbcTemplate jdbc;

  public record NoticeView(Long id, String title, String body, String author, Instant postedAt) {}
  public record CreateNotice(@NotBlank @Size(max=200) String title,
                             @NotBlank @Size(max=5000) String body) {}
  public record ResidentView(Long id, String name, String email, String phone,
                             String flat, String block, boolean owner) {}

  @GetMapping("/notices")
  public List<NoticeView> notices(Authentication authentication) {
    Long societyId = principal(authentication).societyId();
    return jdbc.query("""
        select n.id,n.title,n.body,u.name,n.posted_at
        from notice n join app_user u on u.id=n.posted_by
        where n.society_id=? order by n.posted_at desc
        """, (rs, row) -> new NoticeView(rs.getLong(1), rs.getString(2), rs.getString(3),
        rs.getString(4), rs.getTimestamp(5).toInstant()), societyId);
  }

  @PostMapping("/notices")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('SOCIETY_ADMIN')")
  public NoticeView createNotice(@Valid @RequestBody CreateNotice request, Authentication authentication) {
    NestlyPrincipal p = principal(authentication);
    jdbc.update("insert into notice(society_id,title,body,posted_by,posted_at) values (?,?,?,?,?)",
        p.societyId(), request.title(), request.body(), p.userId(), Timestamp.from(Instant.now()));
    Long id = jdbc.queryForObject("select id from notice where society_id=? and posted_by=? order by id desc limit 1",
        Long.class, p.societyId(), p.userId());
    String author = jdbc.queryForObject("select name from app_user where id=?", String.class, p.userId());
    return new NoticeView(id, request.title(), request.body(), author, Instant.now());
  }

  @GetMapping("/residents")
  public List<ResidentView> residents(Authentication authentication) {
    Long societyId = principal(authentication).societyId();
    return jdbc.query("""
        select r.id,u.name,u.email,u.phone,f.flat_number,b.block_name,r.is_owner
        from resident r
        join app_user u on u.id=r.user_id
        join flat f on f.id=r.flat_id
        join block b on b.id=f.block_id
        where r.society_id=? order by b.block_name,f.flat_number
        """, (rs, row) -> new ResidentView(rs.getLong(1), rs.getString(2), rs.getString(3),
        rs.getString(4), rs.getString(5), rs.getString(6), rs.getBoolean(7)), societyId);
  }

  private NestlyPrincipal principal(Authentication authentication) {
    return (NestlyPrincipal) authentication.getPrincipal();
  }
}

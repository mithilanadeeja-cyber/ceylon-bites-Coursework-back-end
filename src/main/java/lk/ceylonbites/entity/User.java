package lk.ceylonbites.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity. Controllers expose DTOs, never persistence entities. */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Role role = Role.CUSTOMER;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();
}

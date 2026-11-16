package lk.ceylonbites.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity. Controllers expose DTOs, never persistence entities. */
@Entity
@Table(
    name = "customer_orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "request_key"}))
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private OrderStatus status = OrderStatus.PLACED;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal deliveryFee;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(nullable = false, length = 300)
  private String address;

  @Column(nullable = false, length = 25)
  private String phone;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false, length = 64)
  private String requestKey;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, optional = false)
  private Payment payment;
}

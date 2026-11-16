package lk.ceylonbites.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity. Controllers expose DTOs, never persistence entities. */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private CustomerOrder order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "food_id", nullable = false)
  private FoodItem food;

  @Column(nullable = false, length = 80)
  private String foodName;

  @Column(nullable = false, length = 500)
  private String image;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private int quantity;
}
